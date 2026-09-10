package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.variable.MapDelegateVariableContainer;
import org.flowable.engine.ManagementService;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 根据当前 BPMN 节点与流程变量预测后续用户任务。
 */
@Component
public class WorkflowBpmnNavigator {

    private final ManagementService managementService;

    public WorkflowBpmnNavigator(ManagementService managementService) {
        this.managementService = managementService;
    }

    public List<UserTask> nextUserTasks(FlowElement currentElement, BpmnModel bpmnModel,
                                        Map<String, Object> variables) {
        return userTasks(currentElement, bpmnModel, variables, true);
    }

    /**
     * 模拟从指定节点开始的完整可达审批路径，用于流程发起前的审批时间线和自选审批人配置。
     */
    public List<UserTask> reachableUserTasks(FlowElement currentElement, BpmnModel bpmnModel,
                                             Map<String, Object> variables) {
        return userTasks(currentElement, bpmnModel, variables, false);
    }

    public boolean skipExpressionMatches(FlowElement element, Map<String, Object> variables) {
        if (!(element instanceof UserTask userTask)) {
            return false;
        }
        String expression = userTask.getSkipExpression();
        return expression != null && !expression.isBlank()
                && evaluate(expression, variables == null ? Collections.emptyMap() : variables);
    }

    /**
     * 按 BPMN 结构（不计算条件表达式）查找目标节点之后仍在运行的用户任务，
     * 用于驳回/退回时挑选需要一起撤回到目标节点的活动任务。
     */
    public List<UserTask> structurallyReachableUserTasks(FlowElement source, BpmnModel bpmnModel,
                                                         Set<String> runningTaskKeys) {
        Map<String, UserTask> result = new LinkedHashMap<>();
        collectStructurally(source, bpmnModel, runningTaskKeys == null ? Set.of() : runningTaskKeys,
                new HashSet<>(), result);
        return new ArrayList<>(result.values());
    }

    private void collectStructurally(FlowElement currentElement, BpmnModel bpmnModel,
                                     Set<String> runningTaskKeys, Set<String> visitedFlows,
                                     Map<String, UserTask> result) {
        if (!(currentElement instanceof FlowNode flowNode)) {
            return;
        }
        for (SequenceFlow flow : flowNode.getOutgoingFlows()) {
            if (flow.getId() != null && !visitedFlows.add(flow.getId())) {
                continue;
            }
            FlowElement target = bpmnModel.getFlowElement(flow.getTargetRef());
            if (target == null || target instanceof EndEvent) {
                continue;
            }
            if (target instanceof UserTask userTask) {
                if (runningTaskKeys.contains(userTask.getId())) {
                    result.putIfAbsent(userTask.getId(), userTask);
                }
                collectStructurally(userTask, bpmnModel, runningTaskKeys, visitedFlows, result);
            } else {
                collectStructurally(target, bpmnModel, runningTaskKeys, visitedFlows, result);
            }
        }
    }

    private List<UserTask> userTasks(FlowElement currentElement, BpmnModel bpmnModel,
                                     Map<String, Object> variables, boolean stopAtFirstUserTasks) {
        Map<String, UserTask> result = new LinkedHashMap<>();
        collect(currentElement, bpmnModel, variables == null ? Collections.emptyMap() : variables,
                stopAtFirstUserTasks, new HashSet<>(), result);
        return new ArrayList<>(result.values());
    }

    private void collect(FlowElement currentElement, BpmnModel bpmnModel, Map<String, Object> variables,
                         boolean stopAtFirstUserTasks, Set<String> visitedFlows,
                         Map<String, UserTask> result) {
        for (SequenceFlow flow : outgoingFlows(currentElement, variables)) {
            if (flow.getId() != null && !visitedFlows.add(flow.getId())) {
                continue;
            }
            FlowElement target = bpmnModel.getFlowElement(flow.getTargetRef());
            if (target == null || target instanceof EndEvent) {
                continue;
            }
            if (target instanceof UserTask userTask) {
                result.putIfAbsent(userTask.getId(), userTask);
                if (!stopAtFirstUserTasks) {
                    collect(userTask, bpmnModel, variables, false, visitedFlows, result);
                }
            } else {
                collect(target, bpmnModel, variables, stopAtFirstUserTasks, visitedFlows, result);
            }
        }
    }

    private List<SequenceFlow> outgoingFlows(FlowElement element, Map<String, Object> variables) {
        if (!(element instanceof FlowNode flowNode)) {
            return Collections.emptyList();
        }
        if (!(element instanceof Gateway gateway)) {
            return flowNode.getOutgoingFlows();
        }
        if (gateway instanceof ExclusiveGateway) {
            SequenceFlow matched = gateway.getOutgoingFlows().stream()
                    .filter(flow -> !Objects.equals(gateway.getDefaultFlow(), flow.getId()))
                    .filter(flow -> evaluate(flow.getConditionExpression(), variables))
                    .findFirst().orElse(null);
            if (matched == null) {
                matched = defaultOrOnlyFlow(gateway);
            }
            return matched == null ? Collections.emptyList() : List.of(matched);
        }
        if (gateway instanceof InclusiveGateway) {
            List<SequenceFlow> matched = gateway.getOutgoingFlows().stream()
                    .filter(flow -> !Objects.equals(gateway.getDefaultFlow(), flow.getId()))
                    .filter(flow -> evaluate(flow.getConditionExpression(), variables))
                    .toList();
            if (!matched.isEmpty()) {
                return matched;
            }
            SequenceFlow fallback = defaultOrOnlyFlow(gateway);
            return fallback == null ? Collections.emptyList() : List.of(fallback);
        }
        return gateway.getOutgoingFlows();
    }

    private SequenceFlow defaultOrOnlyFlow(Gateway gateway) {
        SequenceFlow defaultFlow = gateway.getOutgoingFlows().stream()
                .filter(flow -> Objects.equals(gateway.getDefaultFlow(), flow.getId()))
                .findFirst().orElse(null);
        if (defaultFlow == null && gateway.getOutgoingFlows().size() == 1) {
            return gateway.getOutgoingFlows().get(0);
        }
        return defaultFlow;
    }

    private boolean evaluate(String expressionText, Map<String, Object> variables) {
        if (expressionText == null || expressionText.isBlank()) {
            return false;
        }
        try {
            VariableContainer container = new MapDelegateVariableContainer(variables, VariableContainer.empty());
            Object value = managementService.executeCommand(context -> {
                Expression expression = CommandContextUtil.getProcessEngineConfiguration()
                        .getExpressionManager().createExpression(expressionText);
                return expression.getValue(container);
            });
            return Boolean.TRUE.equals(value);
        } catch (RuntimeException ignored) {
            // 缺失变量代表该条件当前不成立；预测接口不应因此中断整个审批页面。
            return false;
        }
    }
}

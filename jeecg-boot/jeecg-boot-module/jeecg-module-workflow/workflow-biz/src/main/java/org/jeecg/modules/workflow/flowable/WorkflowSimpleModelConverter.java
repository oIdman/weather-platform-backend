package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.IntermediateCatchEvent;
import org.flowable.bpmn.model.IOParameter;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ReceiveTask;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.bpmn.model.UserTask;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_NAMESPACE;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_PREFIX;

/**
 * Converts the RuoYi/Yudao simplified process JSON tree into an executable Flowable BPMN model.
 *
 * <p>The JSON contract deliberately remains map based. This keeps the portable workflow module
 * independent from either product's web DTO classes while preserving their node type numbers.</p>
 */
@Component
public class WorkflowSimpleModelConverter {
    private static final int START_NODE = 0;
    private static final int END_NODE = 1;
    private static final int START_USER_NODE = 10;
    private static final int APPROVE_NODE = 11;
    private static final int COPY_NODE = 12;
    private static final int TRANSACTOR_NODE = 13;
    private static final int DELAY_TIMER_NODE = 14;
    private static final int TRIGGER_NODE = 15;
    private static final int CHILD_PROCESS_NODE = 20;
    private static final int CONDITION_NODE = 50;
    private static final int CONDITION_BRANCH_NODE = 51;
    private static final int PARALLEL_BRANCH_NODE = 52;
    private static final int INCLUSIVE_BRANCH_NODE = 53;
    private static final int ROUTER_BRANCH_NODE = 54;
    private static final String CANDIDATE_STRATEGY = WorkflowProcessConstants.EXTENSION_CANDIDATE_STRATEGY;
    private static final String CANDIDATE_PARAM = WorkflowProcessConstants.EXTENSION_CANDIDATE_PARAM;
    private static final String APPROVE_METHOD = "approveMethod";
    private static final String APPROVE_RATIO = "approveRatio";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CANDIDATE_RESOLVER_EXPRESSION =
            "${workflowCandidateResolver.resolveOne(execution)}";
    private static final String CANDIDATE_RESOLVER_ALL_EXPRESSION =
            "${workflowCandidateResolver.resolveAll(execution)}";
    private static final String START_USER_SELECT_ASSIGNEES = "PROCESS_START_USER_SELECT_ASSIGNEES";
    private static final String APPROVE_USER_SELECT_ASSIGNEES = "PROCESS_APPROVE_USER_SELECT_ASSIGNEES";

    private final AtomicInteger generatedId = new AtomicInteger();
    private final Set<String> usedIds = new HashSet<>();

    public String convert(String processId, String processName, Map<String, Object> simpleModel) {
        if (!StringUtils.hasText(processId)) {
            throw new JeecgBootException("流程标识不能为空");
        }
        if (simpleModel == null || simpleModel.isEmpty()) {
            throw new JeecgBootException("简易流程设计不能为空");
        }
        generatedId.set(0);
        usedIds.clear();

        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.setTargetNamespace("http://jeecg.org/workflow");
        Process process = new Process();
        process.setId(processId);
        process.setName(StringUtils.hasText(processName) ? processName : processId);
        process.setExecutable(true);
        bpmnModel.addProcess(process);

        StartEvent startEvent = new StartEvent();
        startEvent.setId(uniqueId("StartEvent_1"));
        startEvent.setName("开始");
        process.addFlowElement(startEvent);

        EndEvent endEvent = new EndEvent();
        endEvent.setId(uniqueId("EndEvent_1"));
        endEvent.setName("结束");
        process.addFlowElement(endEvent);

        Map<String, Object> firstNode = unwrapStartNode(simpleModel);
        Fragment fragment = buildChain(process, firstNode);
        if (fragment == null) {
            connect(process, startEvent.getId(), endEvent.getId(), null, null);
        } else {
            connect(process, startEvent.getId(), fragment.entryId(), null, null);
            for (String exitId : fragment.exitIds()) {
                connect(process, exitId, endEvent.getId(), null, null);
            }
        }
        return new String(new BpmnXMLConverter().convertToXML(bpmnModel), StandardCharsets.UTF_8);
    }

    public Map<String, Object> defaultSimpleModel() {
        Map<String, Object> end = new LinkedHashMap<>();
        end.put("id", "EndNode");
        end.put("name", "结束");
        end.put("type", END_NODE);
        Map<String, Object> startUser = new LinkedHashMap<>();
        startUser.put("id", "StartUserNode");
        startUser.put("name", "发起人");
        startUser.put("type", START_USER_NODE);
        startUser.put("childNode", end);
        return startUser;
    }

    private Fragment buildChain(Process process, Map<String, Object> node) {
        if (!isValid(node)) {
            return null;
        }
        int type = integer(node.get("type"), -1);
        if (type == START_NODE || type == START_USER_NODE) {
            return buildChain(process, map(node.get("childNode")));
        }
        if (type == END_NODE) {
            return null;
        }
        if (type == CONDITION_NODE) {
            return buildChain(process, map(node.get("childNode")));
        }
        if (type == CONDITION_BRANCH_NODE || type == PARALLEL_BRANCH_NODE
                || type == INCLUSIVE_BRANCH_NODE || type == ROUTER_BRANCH_NODE) {
            return buildBranch(process, node, type);
        }

        FlowNode flowNode = buildFlowNode(node, type);
        process.addFlowElement(flowNode);
        addTimeoutBoundary(process, node, flowNode);
        boolean httpCallback = type == TRIGGER_NODE
                && integer(map(node.get("triggerSetting")).get("type"), 1) == 2;
        Fragment child = buildChain(process, map(node.get("childNode")));
        if (httpCallback) {
            ReceiveTask receiveTask = new ReceiveTask();
            receiveTask.setId(flowNode.getId() + "_callback");
            receiveTask.setName("HTTP 回调等待");
            process.addFlowElement(receiveTask);
            connect(process, flowNode.getId(), receiveTask.getId(), null, null);
            if (child != null) {
                connect(process, receiveTask.getId(), child.entryId(), null, null);
                return new Fragment(flowNode.getId(), child.exitIds());
            }
            return new Fragment(flowNode.getId(), List.of(receiveTask.getId()));
        }
        if (child != null) {
            connect(process, flowNode.getId(), child.entryId(), null, null);
            return new Fragment(flowNode.getId(), child.exitIds());
        }
        return new Fragment(flowNode.getId(), List.of(flowNode.getId()));
    }

    private void configureCallActivityVariables(CallActivity activity, Map<String, Object> setting) {
        boolean async = bool(setting.get("async"));
        activity.setAsynchronous(async);
        String processInstanceName = text(setting.get("calledProcessDefinitionName"));
        if (processInstanceName != null && !processInstanceName.isBlank()) {
            activity.setProcessInstanceName(processInstanceName);
        }
        List<IOParameter> inParameters = new ArrayList<>();
        List<String> inTargets = new ArrayList<>();
        for (Map<String, Object> parameter : ioMaps(setting.get("inVariables"))) {
            IOParameter io = buildIoParameter(parameter);
            if (io != null && !inTargets.contains(io.getTarget())) {
                inParameters.add(io);
                inTargets.add(io.getTarget());
            }
        }
        addIoParameter(inParameters, inTargets, WorkflowProcessConstants.VARIABLE_STATUS,
                WorkflowProcessConstants.VARIABLE_STATUS, null);
        addIoParameter(inParameters, inTargets, "_workflowStartUserId", "_workflowStartUserId", null);
        addIoParameter(inParameters, inTargets, "_workflowTenantId", "_workflowTenantId", null);
        if (!inParameters.isEmpty()) {
            activity.setInParameters(inParameters);
        }
        if (!async) {
            List<IOParameter> outParameters = new ArrayList<>();
            for (Map<String, Object> parameter : ioMaps(setting.get("outVariables"))) {
                IOParameter io = buildIoParameter(parameter);
                if (io != null) {
                    outParameters.add(io);
                }
            }
            if (!outParameters.isEmpty()) {
                activity.setOutParameters(outParameters);
            }
        }
    }

    private List<Map<String, Object>> ioMaps(Object value) {
        if (value instanceof String text) {
            if (text.isBlank()) {
                return List.of();
            }
            try {
                return objectMapper.readValue(text,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        });
            } catch (Exception exception) {
                throw new JeecgBootException("子流程变量配置 JSON 格式不正确", exception);
            }
        }
        return maps(value);
    }

    private IOParameter buildIoParameter(Map<String, Object> parameter) {
        String source = firstText(parameter, "source", "sourceVariable");
        String target = firstText(parameter, "target", "targetVariable");
        String sourceExpression = firstText(parameter, "sourceExpression", "expression");
        if ((source == null && sourceExpression == null) || target == null) {
            return null;
        }
        IOParameter io = new IOParameter();
        if (sourceExpression != null) {
            io.setSourceExpression(sourceExpression);
        } else {
            io.setSource(source);
        }
        io.setTarget(target);
        return io;
    }

    private void addIoParameter(List<IOParameter> parameters, List<String> targets,
                                String source, String target, String sourceExpression) {
        if (targets.contains(target)) {
            return;
        }
        IOParameter io = new IOParameter();
        io.setSource(source);
        io.setTarget(target);
        if (sourceExpression != null) {
            io.setSourceExpression(sourceExpression);
        }
        parameters.add(io);
        targets.add(target);
    }

    private void addTimeoutBoundary(Process process, Map<String, Object> node, FlowNode flowNode) {
        if (!(flowNode instanceof UserTask userTask)) {
            return;
        }
        Map<String, Object> timeout = map(node.get("timeoutHandler"));
        if (!bool(timeout.get("enable"))) {
            return;
        }
        int handlerType = integer(timeout.get("type"), WorkflowProcessConstants.TIMEOUT_HANDLER_APPROVE);
        int maxRemindCount = integer(timeout.get("maxRemindCount"), 1);
        String duration = text(timeout.get("timeDuration"));
        if (duration == null || duration.isBlank()) {
            throw new JeecgBootException("审批节点启用了超时处理但未设置超时时间");
        }
        if (handlerType < WorkflowProcessConstants.TIMEOUT_HANDLER_REMINDER
                || handlerType > WorkflowProcessConstants.TIMEOUT_HANDLER_REJECT) {
            throw new JeecgBootException("不支持的审批超时处理类型：" + handlerType);
        }
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setId(uniqueId("BoundaryEvent_" + userTask.getId()));
        boundaryEvent.setName("审批超时");
        boundaryEvent.setAttachedToRef(userTask);
        boundaryEvent.setCancelActivity(false);
        TimerEventDefinition timer = new TimerEventDefinition();
        timer.setTimeDuration(duration);
        if (handlerType == WorkflowProcessConstants.TIMEOUT_HANDLER_REMINDER && maxRemindCount > 1) {
            timer.setTimeCycle("R" + maxRemindCount + "/" + duration);
        }
        boundaryEvent.addEventDefinition(timer);
        addExtensionElement(boundaryEvent, WorkflowProcessConstants.EXTENSION_BOUNDARY_EVENT_TYPE,
                String.valueOf(WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT));
        addExtensionElement(boundaryEvent, WorkflowProcessConstants.EXTENSION_TIMEOUT_HANDLER_TYPE,
                String.valueOf(handlerType));
        process.addFlowElement(boundaryEvent);
    }

    private Fragment buildBranch(Process process, Map<String, Object> node, int type) {
        FlowNode split = switch (type) {
            case PARALLEL_BRANCH_NODE -> new ParallelGateway();
            case INCLUSIVE_BRANCH_NODE -> new InclusiveGateway();
            default -> new ExclusiveGateway();
        };
        split.setId(uniqueId(string(node.get("id"), "Gateway")));
        split.setName(string(node.get("name"), "分支"));
        process.addFlowElement(split);

        FlowNode join = switch (type) {
            case PARALLEL_BRANCH_NODE -> new ParallelGateway();
            case INCLUSIVE_BRANCH_NODE -> new InclusiveGateway();
            default -> new ExclusiveGateway();
        };
        join.setId(uniqueId(split.getId() + "_join"));
        join.setName(split.getName() + "汇合");
        process.addFlowElement(join);

        List<Map<String, Object>> branches = maps(node.get("conditionNodes"));
        if (branches.isEmpty()) {
            branches = maps(node.get("branches"));
        }
        if (type == ROUTER_BRANCH_NODE && branches.isEmpty()) {
            branches = maps(node.get("routerSettings"));
        }
        if (branches.isEmpty()) {
            throw new JeecgBootException("分支节点至少需要一个分支：" + split.getName());
        }

        boolean defaultAssigned = false;
        for (int index = 0; index < branches.size(); index++) {
            Map<String, Object> branch = branches.get(index);
            Map<String, Object> branchNode = map(branch.get("childNode"));
            if (!isValid(branchNode)) {
                branchNode = map(branch.get("node"));
            }
            Fragment branchFragment = buildChain(process, branchNode);
            String condition = type == PARALLEL_BRANCH_NODE ? null : conditionExpression(branch);
            boolean defaultBranch = type != PARALLEL_BRANCH_NODE
                    && (!StringUtils.hasText(condition) || bool(branch.get("defaultFlow"))
                    || bool(branch.get("defaultBranch")));
            String target = branchFragment == null ? join.getId() : branchFragment.entryId();
            SequenceFlow flow = connect(process, split.getId(), target,
                    defaultBranch ? null : condition, string(branch.get("name"), "分支" + (index + 1)));
            if (defaultBranch && !defaultAssigned && split instanceof ExclusiveGateway exclusiveGateway) {
                exclusiveGateway.setDefaultFlow(flow.getId());
                defaultAssigned = true;
            }
            if (branchFragment != null) {
                for (String exitId : branchFragment.exitIds()) {
                    connect(process, exitId, join.getId(), null, null);
                }
            }
        }

        Fragment child = buildChain(process, map(node.get("childNode")));
        if (child != null) {
            connect(process, join.getId(), child.entryId(), null, null);
            return new Fragment(split.getId(), child.exitIds());
        }
        return new Fragment(split.getId(), List.of(join.getId()));
    }

    private FlowNode buildFlowNode(Map<String, Object> node, int type) {
        String id = uniqueId(string(node.get("id"), "Node"));
        String name = string(node.get("name"), "流程节点");
        FlowNode result;
        if (type == APPROVE_NODE || type == TRANSACTOR_NODE) {
            UserTask userTask = new UserTask();
            userTask.setId(id);
            userTask.setName(name);
            configureUserTask(userTask, node);
            result = userTask;
        } else if (type == COPY_NODE) {
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setImplementationType("delegateExpression");
            serviceTask.setImplementation("${workflowCopyTaskDelegate}");
            configureCopyNode(serviceTask, node);
            result = serviceTask;
        } else if (type == DELAY_TIMER_NODE) {
            IntermediateCatchEvent timer = new IntermediateCatchEvent();
            TimerEventDefinition definition = new TimerEventDefinition();
            configureTimer(definition, map(node.get("delaySetting")));
            timer.addEventDefinition(definition);
            result = timer;
        } else if (type == TRIGGER_NODE) {
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setId(id);
            serviceTask.setName(name);
            serviceTask.setImplementationType("delegateExpression");
            serviceTask.setImplementation("${workflowTriggerTaskDelegate}");
            Map<String, Object> triggerSetting = map(node.get("triggerSetting"));
            String callbackTaskKey = integer(triggerSetting.get("type"), 1) == 2 ? id + "_callback" : null;
            configureTriggerNode(serviceTask, node, callbackTaskKey);
            result = serviceTask;
        } else if (type == CHILD_PROCESS_NODE) {
            Map<String, Object> setting = map(node.get("childProcessSetting"));
            String calledElement = firstText(setting, "calledElement", "calledProcessDefinitionKey",
                    "processDefinitionKey", "processKey");
            if (!StringUtils.hasText(calledElement)) {
                throw new JeecgBootException("子流程节点未配置流程标识：" + string(node.get("name"), "子流程"));
            }
            CallActivity activity = new CallActivity();
            activity.setCalledElement(calledElement);
            activity.setCalledElementType("key");
            configureCallActivityVariables(activity, setting);
            result = activity;
        } else {
            throw new JeecgBootException("暂不支持的简易流程节点类型：" + type);
        }
        result.setId(id);
        result.setName(name);
        return result;
    }

    private void configureUserTask(UserTask task, Map<String, Object> node) {
        String assignee = firstText(node, "assignee", "assigneeUserId");
        List<String> candidateUsers = strings(node.get("candidateUsers"));
        List<String> candidateGroups = strings(node.get("candidateGroups"));
        int strategy = integer(node.get("candidateStrategy"), -1);
        int approveMethod = integer(node.get(APPROVE_METHOD), 1);
        int approveRatio = integer(node.get(APPROVE_RATIO), 100);
        String rawCandidateParam = text(node.get("candidateParam"));
        List<String> candidateParam = strings(node.get("candidateParam"));
        int approveType = integer(node.get("approveType"), 1);
        if (approveType == 2 || approveType == 3) {
            assignee = "${_workflowStartUserId}";
            candidateUsers = List.of();
            candidateGroups = List.of();
            strategy = -1;
            approveMethod = 1;
            rawCandidateParam = null;
            candidateParam = List.of();
        }
        if (candidateParam.isEmpty() && StringUtils.hasText(rawCandidateParam)) {
            candidateParam = splitComma(rawCandidateParam);
        }
        if (strategy == 30 && candidateUsers.isEmpty()) {
            candidateUsers = candidateParam;
        } else if (strategy == 10 && candidateGroups.isEmpty()) {
            candidateGroups = candidateParam;
        } else if (strategy == 36 && !StringUtils.hasText(assignee)) {
            assignee = "${_workflowStartUserId}";
        } else if (strategy == 35 && !StringUtils.hasText(assignee)) {
            assignee = selectedAssigneeExpression(START_USER_SELECT_ASSIGNEES, task.getId());
        } else if (strategy == 34 && !StringUtils.hasText(assignee)) {
            assignee = selectedAssigneeExpression(APPROVE_USER_SELECT_ASSIGNEES, task.getId());
        } else if ((strategy == 32 || strategy == 33) && !StringUtils.hasText(assignee)) {
            assignee = "${_workflowStartUserId}";
        }
        validateCandidateParam(strategy, rawCandidateParam);
        boolean multiInstance = approveMethod == 2 || approveMethod == 3 || approveMethod == 4;
        if (multiInstance) {
            // The runtime engine fills the collection from the configured candidate strategy.
            // Each Flowable child task then receives one item from that collection as assignee.
            assignee = "${" + WorkflowProcessConstants.multiInstanceAssigneeVariable(task.getId()) + "}";
            configureMultiInstance(task, approveMethod, approveRatio);
            candidateUsers = List.of();
            candidateGroups = List.of();
        } else if (approveMethod == 1 && strategy >= 0) {
            if (!StringUtils.hasText(assignee)) {
                assignee = CANDIDATE_RESOLVER_EXPRESSION;
            }
            candidateUsers = List.of();
            candidateGroups = List.of();
        }
        if (StringUtils.hasText(assignee)) {
            task.setAssignee(assignee);
        }
        if (!candidateUsers.isEmpty()) {
            task.setCandidateUsers(candidateUsers);
        }
        if (!candidateGroups.isEmpty()) {
            task.setCandidateGroups(candidateGroups);
        }
        addExtensionElement(task, CANDIDATE_STRATEGY, strategy < 0 ? null : String.valueOf(strategy));
        addExtensionElement(task, CANDIDATE_PARAM, StringUtils.hasText(rawCandidateParam)
                ? rawCandidateParam : String.join(",", candidateParam));
        configureAssignmentHandlers(task, node);
        configureRejectHandler(task, node);
        configureSkipExpression(task, node);
        configureOperationSettings(task, node);
        configureTaskListeners(task, node);
        configureExecutionListeners(task, node);
        addExtensionElement(task, APPROVE_METHOD, String.valueOf(approveMethod));
        if (approveMethod == 2) {
            if (approveRatio < 1 || approveRatio > 100) {
                throw new JeecgBootException("会签通过比例必须在 1 到 100 之间");
            }
            addExtensionElement(task, APPROVE_RATIO, String.valueOf(approveRatio));
        }
        if (!StringUtils.hasText(task.getAssignee()) && candidateUsers.isEmpty() && candidateGroups.isEmpty()) {
            throw new JeecgBootException("审批节点未配置审批人：" + string(node.get("name"), "审批"));
        }
    }

    private void configureAssignmentHandlers(UserTask task, Map<String, Object> node) {
        Map<String, Object> emptyHandler = map(node.get("assignEmptyHandler"));
        int emptyHandlerType = integer(emptyHandler.get("type"), WorkflowProcessConstants.ASSIGN_EMPTY_APPROVE);
        if (emptyHandlerType < WorkflowProcessConstants.ASSIGN_EMPTY_APPROVE
                || emptyHandlerType > WorkflowProcessConstants.ASSIGN_EMPTY_MANAGER) {
            throw new JeecgBootException("审批人为空处理类型不正确：" + emptyHandlerType);
        }
        List<String> emptyUserIds = strings(emptyHandler.get("userIds"));
        if (emptyHandlerType == WorkflowProcessConstants.ASSIGN_EMPTY_USER && emptyUserIds.isEmpty()) {
            throw new JeecgBootException("审批人为空时请选择指定审批人员");
        }
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE,
                String.valueOf(emptyHandlerType));
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_USER_IDS,
                String.join(",", emptyUserIds));

        int startUserHandlerType = integer(node.get("assignStartUserHandlerType"),
                WorkflowProcessConstants.ASSIGN_START_USER_AUDIT);
        if (startUserHandlerType < WorkflowProcessConstants.ASSIGN_START_USER_AUDIT
                || startUserHandlerType > WorkflowProcessConstants.ASSIGN_START_USER_DEPARTMENT_LEADER) {
            throw new JeecgBootException("审批人与发起人相同处理类型不正确：" + startUserHandlerType);
        }
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_ASSIGN_START_USER_HANDLER_TYPE,
                String.valueOf(startUserHandlerType));
    }

    private void configureRejectHandler(UserTask task, Map<String, Object> node) {
        Map<String, Object> handler = map(node.get("rejectHandler"));
        int type = integer(handler.get("type"), WorkflowProcessConstants.REJECT_HANDLER_FINISH_PROCESS);
        if (type != WorkflowProcessConstants.REJECT_HANDLER_FINISH_PROCESS
                && type != WorkflowProcessConstants.REJECT_HANDLER_RETURN_USER_TASK) {
            throw new JeecgBootException("拒绝后处理类型不正确：" + type);
        }
        String returnNodeId = firstText(handler, "returnNodeId", "targetTaskDefinitionKey",
                "returnTaskId", "returnNodeKey");
        if (type == WorkflowProcessConstants.REJECT_HANDLER_RETURN_USER_TASK && !StringUtils.hasText(returnNodeId)) {
            throw new JeecgBootException("拒绝后选择驳回到指定节点时，退回节点不能为空");
        }
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_REJECT_HANDLER_TYPE, String.valueOf(type));
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_REJECT_RETURN_TASK_ID, returnNodeId);
    }

    private void configureOperationSettings(UserTask task, Map<String, Object> node) {
        int approveType = integer(node.get("approveType"), 1);
        if (approveType > 0) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_APPROVE_TYPE,
                    String.valueOf(approveType));
        }
        Collection<String> buttons = strings(node.get("enabledButtons"));
        if (!buttons.isEmpty()) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_ENABLED_BUTTONS,
                    String.join(",", buttons));
        }
        Object buttonSettings = node.get("buttonsSetting");
        if (buttonSettings instanceof Collection<?>) {
            try {
                addExtensionElement(task, WorkflowProcessConstants.EXTENSION_BUTTONS_SETTING,
                        objectMapper.writeValueAsString(buttonSettings));
            } catch (Exception exception) {
                throw new JeecgBootException("操作按钮配置无法序列化", exception);
            }
        }
        if (bool(node.get("reasonRequire"))) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_REASON_REQUIRE, "true");
        }
        if (bool(node.get("signEnable"))) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_SIGN_ENABLE, "true");
        }
        configureFieldPermissions(task, node);
    }

    private void configureFieldPermissions(UserTask task, Map<String, Object> node) {
        Object raw = node.get("fieldsPermission");
        if (raw instanceof String rawText && !rawText.isBlank()) {
            try {
                raw = objectMapper.readValue(rawText, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                });
            } catch (Exception exception) {
                throw new JeecgBootException("字段权限 JSON 格式不正确", exception);
            }
        } else if (raw instanceof String) {
            return;
        }
        Collection<?> collection;
        if (raw instanceof Collection<?> parsed) {
            collection = parsed;
        } else {
            Object alternative = node.get("formFieldsPermission");
            if (!(alternative instanceof Collection<?> parsedAlternative)) {
                return;
            }
            collection = parsedAlternative;
        }
        Map<String, String> permissionMap = new LinkedHashMap<>();
        for (Object item : collection) {
            Map<String, Object> setting = map(item);
            String field = firstText(setting, "field", "name", "fieldName");
            String permission = firstText(setting, "permission", "auth", "type");
            if (field == null || permission == null) {
                continue;
            }
            String normalized = permission.toUpperCase(java.util.Locale.ROOT);
            if ("READONLY".equals(normalized)) {
                normalized = "READ";
            } else if ("EDITABLE".equals(normalized)) {
                normalized = "WRITE";
            } else if ("HIDE".equals(normalized)) {
                normalized = "NONE";
            }
            if (normalized.equals("READ") || normalized.equals("WRITE") || normalized.equals("NONE")) {
                permissionMap.put(field, normalized);
            }
        }
        if (!permissionMap.isEmpty()) {
            try {
                addExtensionElement(task, WorkflowProcessConstants.EXTENSION_FORM_FIELD_PERMISSION,
                        objectMapper.writeValueAsString(permissionMap));
            } catch (Exception exception) {
                throw new JeecgBootException("字段权限无法序列化", exception);
            }
        }
    }

    private void configureTaskListeners(UserTask task, Map<String, Object> node) {
        List<FlowableListener> configuredListeners = new ArrayList<>();
        addHttpTaskListener(configuredListeners, node.get("taskCreateListener"), "create");
        addHttpTaskListener(configuredListeners, node.get("taskAssignListener"), "assignment");
        addHttpTaskListener(configuredListeners, node.get("taskCompleteListener"), "complete");
        Object raw = node.get("taskListeners");
        if (!(raw instanceof String)) {
            raw = node.get("listeners");
        }
        List<Map<String, Object>> settings = new ArrayList<>();
        if (raw instanceof String) {
            String rawText = (String) raw;
            if (rawText.isBlank()) {
                task.setTaskListeners(configuredListeners);
                return;
            }
            try {
                settings.addAll(objectMapper.readValue(rawText,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        }));
            } catch (Exception exception) {
                throw new JeecgBootException("任务监听器 JSON 格式不正确", exception);
            }
        } else if (raw instanceof Collection<?> collection) {
            settings.addAll(maps(raw));
        } else {
            if (!configuredListeners.isEmpty()) {
                task.setTaskListeners(configuredListeners);
            }
            return;
        }
        List<FlowableListener> listeners = new ArrayList<>(configuredListeners);
        for (Map<String, Object> setting : settings) {
            if (Boolean.FALSE.equals(setting.get("enable"))) {
                continue;
            }
            String event = firstText(setting, "eventName", "event");
            String implementationType = firstText(setting, "implementationType", "valueType", "listenerType");
            String implementation = firstText(setting, "implementation", "value", "delegateExpression", "className");
            if (event == null || implementation == null) {
                continue;
            }
            String normalizedEvent = event.replace("task:", "").toLowerCase(java.util.Locale.ROOT);
            String normalizedType = implementationType == null ? "delegateExpression"
                    : implementationType.toLowerCase(java.util.Locale.ROOT);
            FlowableListener listener = new FlowableListener();
            listener.setEvent(normalizedEvent);
            if (normalizedType.contains("delegate") || normalizedType.contains("expression")) {
                listener.setImplementationType("delegateExpression");
                listener.setImplementation(implementation.startsWith("${") ? implementation
                        : "${" + implementation + "}");
            } else {
                listener.setImplementationType("class");
                listener.setImplementation(implementation);
            }
            listeners.add(listener);
        }
        if (!listeners.isEmpty()) {
            List<FlowableListener> existing = task.getTaskListeners() == null
                    ? new ArrayList<>() : new ArrayList<>(task.getTaskListeners());
            existing.addAll(listeners);
            task.setTaskListeners(existing);
        }
    }

    private void addHttpTaskListener(List<FlowableListener> listeners, Object raw, String event) {
        Map<String, Object> setting = map(raw);
        if (!bool(setting.get("enable"))) {
            return;
        }
        String path = text(setting.get("path"));
        if (!StringUtils.hasText(path)) {
            throw new JeecgBootException("任务" + event + "监听器请求地址不能为空");
        }
        try {
            FlowableListener listener = new FlowableListener();
            listener.setEvent(event);
            listener.setImplementationType("delegateExpression");
            listener.setImplementation("${workflowUserTaskHttpListener}");
            FieldExtension field = new FieldExtension();
            field.setFieldName("listenerConfig");
            field.setStringValue(objectMapper.writeValueAsString(setting));
            listener.getFieldExtensions().add(field);
            listeners.add(listener);
        } catch (Exception exception) {
            throw new JeecgBootException("任务监听器配置无法序列化", exception);
        }
    }

    private void configureExecutionListeners(UserTask task, Map<String, Object> node) {
        Object raw = node.get("executionListeners");
        if (!(raw instanceof String) && raw == null) {
            raw = node.get("executionListener");
        }
        if (!(raw instanceof String) && !(raw instanceof Collection<?>)) {
            return;
        }
        List<Map<String, Object>> settings = new ArrayList<>();
        if (raw instanceof String rawText) {
            if (rawText.isBlank()) {
                return;
            }
            try {
                settings.addAll(objectMapper.readValue(rawText,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                        }));
            } catch (Exception exception) {
                throw new JeecgBootException("执行监听器 JSON 格式不正确", exception);
            }
        } else {
            settings.addAll(maps(raw));
        }
        List<FlowableListener> listeners = new ArrayList<>();
        for (Map<String, Object> setting : settings) {
            if (Boolean.FALSE.equals(setting.get("enable"))) {
                continue;
            }
            String event = firstText(setting, "eventName", "event");
            String implementationType = firstText(setting, "implementationType", "valueType", "listenerType");
            String implementation = firstText(setting, "implementation", "value", "delegateExpression", "className");
            if (event == null || implementation == null) {
                continue;
            }
            FlowableListener listener = new FlowableListener();
            listener.setEvent(event.replace("execution:", "").toLowerCase(java.util.Locale.ROOT));
            String normalizedType = implementationType == null ? "delegateExpression"
                    : implementationType.toLowerCase(java.util.Locale.ROOT);
            if (normalizedType.contains("delegate") || normalizedType.contains("expression")) {
                listener.setImplementationType("delegateExpression");
                listener.setImplementation(implementation.startsWith("${") ? implementation
                        : "${" + implementation + "}");
            } else {
                listener.setImplementationType("class");
                listener.setImplementation(implementation);
            }
            listeners.add(listener);
        }
        if (!listeners.isEmpty()) {
            List<FlowableListener> existing = task.getExecutionListeners() == null
                    ? new ArrayList<>() : new ArrayList<>(task.getExecutionListeners());
            existing.addAll(listeners);
            task.setExecutionListeners(existing);
        }
    }

    private void configureCopyNode(ServiceTask task, Map<String, Object> node) {
        configureSkipExpression(task, node);
        String strategy = text(node.get("candidateStrategy"));
        String candidateParam = text(node.get("candidateParam"));
        if (StringUtils.hasText(candidateParam)) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_CANDIDATE_PARAM, candidateParam);
        }
        if (StringUtils.hasText(strategy)) {
            addExtensionElement(task, WorkflowProcessConstants.EXTENSION_CANDIDATE_STRATEGY, strategy);
        }
    }

    private void configureTriggerNode(ServiceTask task, Map<String, Object> node, String callbackTaskKey) {
        configureSkipExpression(task, node);
        Map<String, Object> trigger = map(node.get("triggerSetting"));
        int type = integer(trigger.get("type"), 1);
        if (type != 1 && type != 2 && type != 10 && type != 11) {
            throw new JeecgBootException("不支持的触发器类型：" + type);
        }
        addExtensionElement(task, WorkflowProcessConstants.EXTENSION_TRIGGER_TYPE, String.valueOf(type));
        Object httpSetting = trigger.get("httpRequestSetting");
        Object formSettings = trigger.get("formSettings");
        Map<String, Object> param = new LinkedHashMap<>();
        if (httpSetting instanceof Map<?, ?> && !((Map<?, ?>) httpSetting).isEmpty()) {
            if (callbackTaskKey != null) {
                Map<String, Object> enriched = new LinkedHashMap<>(map(httpSetting));
                enriched.put("callbackTaskDefineKey", callbackTaskKey);
                param.put("httpRequestSetting", enriched);
            } else {
            param.put("httpRequestSetting", httpSetting);
            }
        }
        if (formSettings instanceof Collection<?> && !((Collection<?>) formSettings).isEmpty()) {
            param.put("formSettings", formSettings);
        }
        if (!param.isEmpty()) {
            try {
                addExtensionElement(task, WorkflowProcessConstants.EXTENSION_TRIGGER_PARAM,
                        objectMapper.writeValueAsString(param));
            } catch (Exception exception) {
                throw new JeecgBootException("触发器参数无法序列化", exception);
            }
        }
    }

    private void configureSkipExpression(UserTask task, Map<String, Object> node) {
        String skipExpression = text(node.get("skipExpression"));
        if (StringUtils.hasText(skipExpression)) {
            if (!skipExpression.startsWith("${") && !skipExpression.startsWith("#{")) {
                skipExpression = "${" + skipExpression + "}";
            }
            task.setSkipExpression(skipExpression);
        }
    }

    private void configureSkipExpression(ServiceTask task, Map<String, Object> node) {
        String skipExpression = text(node.get("skipExpression"));
        if (StringUtils.hasText(skipExpression)) {
            if (!skipExpression.startsWith("${") && !skipExpression.startsWith("#{")) {
                skipExpression = "${" + skipExpression + "}";
            }
            task.setSkipExpression(skipExpression);
        }
    }

    private void validateCandidateParam(int strategy, String candidateParam) {
        if (strategy == 32 || strategy == 33 || strategy == 34 || strategy == 35
                || strategy == 36 || strategy < 0) {
            return;
        }
        if (!StringUtils.hasText(candidateParam)) {
            throw new JeecgBootException("候选人参数不能为空");
        }
        if (strategy == 37 || strategy == 38) {
            positiveLevel(candidateParam, "发起人部门负责人");
        } else if (strategy == 23 || strategy == 51) {
            String[] parts = candidateParam.split("\\|", -1);
            if (parts.length != 2 || !StringUtils.hasText(parts[0])) {
                throw new JeecgBootException("候选人参数格式应为“参数|层级”");
            }
            positiveLevel(parts[1], "候选人策略");
        }
    }

    private int positiveLevel(String value, String label) {
        try {
            int level = Integer.parseInt(value.trim());
            if (level > 0) {
                return level;
            }
        } catch (RuntimeException ignored) {
            // 统一抛出业务错误。
        }
        throw new JeecgBootException(label + "的部门层级必须大于 0");
    }

    private void configureMultiInstance(UserTask task, int approveMethod, int approveRatio) {
        MultiInstanceLoopCharacteristics characteristics = new MultiInstanceLoopCharacteristics();
        String activityId = task.getId();
        // 直接在节点进入时计算，保证退回、循环或组织调整后不会复用流程启动时的旧候选人集合。
        characteristics.setInputDataItem(CANDIDATE_RESOLVER_ALL_EXPRESSION);
        characteristics.setElementVariable(WorkflowProcessConstants.multiInstanceAssigneeVariable(activityId));
        if (approveMethod == 4) {
            characteristics.setSequential(true);
            characteristics.setCompletionCondition("${nrOfCompletedInstances >= nrOfInstances}");
        } else if (approveMethod == 3) {
            characteristics.setSequential(false);
            characteristics.setCompletionCondition("${nrOfCompletedInstances > 0}");
        } else if (approveMethod == 2) {
            characteristics.setSequential(false);
            characteristics.setCompletionCondition(String.format(
                    "${nrOfCompletedInstances/nrOfInstances >= %.2f}", approveRatio / 100D));
        }
        task.setLoopCharacteristics(characteristics);
    }

    private String selectedAssigneeExpression(String variableName, String activityId) {
        return "${" + variableName + "['" + activityId.replace("'", "") + "'][0]}";
    }

    private void addExtensionElement(FlowElement element, String name, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
        extensionElement.setNamespacePrefix(FLOWABLE_EXTENSIONS_PREFIX);
        extensionElement.setName(name);
        extensionElement.setElementText(value);
        element.addExtensionElement(extensionElement);
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private void configureTimer(TimerEventDefinition timer, Map<String, Object> setting) {
        String date = firstText(setting, "date", "delayDate", "fixedDateTime");
        if (StringUtils.hasText(date)) {
            timer.setTimeDate(date);
            return;
        }
        String duration = firstText(setting, "duration", "delayDuration");
        if (!StringUtils.hasText(duration)) {
            long amount = longValue(setting.get("delayTime"), longValue(setting.get("value"), 1));
            String unit = firstText(setting, "delayUnit", "unit");
            duration = toIsoDuration(amount, unit);
        }
        timer.setTimeDuration(duration);
    }

    private String conditionExpression(Map<String, Object> branch) {
        Map<String, Object> setting = map(branch.get("conditionSetting"));
        String expression = firstText(setting, "conditionExpression", "expression");
        if (!StringUtils.hasText(expression)) {
            expression = firstText(branch, "conditionExpression", "expression");
        }
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        expression = expression.trim();
        return expression.startsWith("${") || expression.startsWith("#{")
                ? expression : "${" + expression + "}";
    }

    private SequenceFlow connect(Process process, String sourceId, String targetId,
                                 String condition, String name) {
        SequenceFlow flow = new SequenceFlow(sourceId, targetId);
        flow.setId(uniqueId("Flow"));
        if (StringUtils.hasText(name)) {
            flow.setName(name);
        }
        if (StringUtils.hasText(condition)) {
            flow.setConditionExpression(condition);
        }
        process.addFlowElement(flow);
        FlowElement source = process.getFlowElement(sourceId, true);
        FlowElement target = process.getFlowElement(targetId, true);
        if (source instanceof FlowNode sourceNode) {
            sourceNode.getOutgoingFlows().add(flow);
        }
        if (target instanceof FlowNode targetNode) {
            targetNode.getIncomingFlows().add(flow);
        }
        return flow;
    }

    private Map<String, Object> unwrapStartNode(Map<String, Object> model) {
        Map<String, Object> root = map(model.get("rootNode"));
        if (!root.isEmpty()) {
            return root;
        }
        root = map(model.get("process"));
        return root.isEmpty() ? model : root;
    }

    private String uniqueId(String requested) {
        String base = requested == null ? "Element" : requested.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isBlank() || !Character.isLetter(base.charAt(0)) && base.charAt(0) != '_') {
            base = "Element_" + base;
        }
        String candidate = base;
        while (!usedIds.add(candidate)) {
            candidate = base + "_" + generatedId.incrementAndGet();
        }
        return candidate;
    }

    private boolean isValid(Map<String, Object> node) {
        return node != null && !node.isEmpty() && integer(node.get("type"), -1) >= 0;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> source ? (Map<String, Object>) source : Map.of();
    }

    private List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : collection) {
            Map<String, Object> map = map(item);
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        return result;
    }

    private List<String> strings(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().filter(java.util.Objects::nonNull)
                    .map(String::valueOf).map(String::trim).filter(StringUtils::hasText).toList();
        }
        if (value instanceof String text) {
            return splitComma(text);
        }
        return List.of();
    }

    private List<String> splitComma(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim).filter(StringUtils::hasText).toList();
    }

    private String firstText(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            String text = source.get(key) == null ? null : String.valueOf(source.get(key));
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return null;
    }

    private String string(Object value, String defaultValue) {
        String result = value == null ? null : String.valueOf(value);
        return StringUtils.hasText(result) ? result : defaultValue;
    }

    private int integer(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private long longValue(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? defaultValue : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private boolean bool(Object value) {
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String toIsoDuration(long amount, String unit) {
        if (unit == null) {
            return Duration.ofMinutes(amount).toString();
        }
        return switch (unit.toLowerCase()) {
            case "second", "seconds", "秒", "s" -> Duration.ofSeconds(amount).toString();
            case "hour", "hours", "小时", "h" -> Duration.ofHours(amount).toString();
            case "day", "days", "天", "d" -> Duration.ofDays(amount).toString();
            default -> Duration.ofMinutes(amount).toString();
        };
    }

    private record Fragment(String entryId, List<String> exitIds) {
    }
}

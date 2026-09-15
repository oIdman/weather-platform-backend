package org.jeecg.modules.workflow.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 监听 Flowable TIMER_FIRED，处理简易设计器“审批超时”边界事件。
 */
@Slf4j
@Component
public class WorkflowTimeoutEventListener extends AbstractFlowableEngineEventListener {

    private static final Pattern ACTIVITY_ID_PATTERN = Pattern.compile(
            "\\\"activityId\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    private final ObjectProvider<RepositoryService> repositoryServiceProvider;
    private final ObjectProvider<TaskService> taskServiceProvider;
    private final ObjectProvider<RuntimeService> runtimeServiceProvider;
    private final ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider;

    public WorkflowTimeoutEventListener(ObjectProvider<RepositoryService> repositoryServiceProvider,
                                        ObjectProvider<TaskService> taskServiceProvider,
                                        ObjectProvider<RuntimeService> runtimeServiceProvider,
                                        ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider) {
        super(Set.of(FlowableEngineEventType.TIMER_FIRED));
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.taskServiceProvider = taskServiceProvider;
        this.runtimeServiceProvider = runtimeServiceProvider;
        this.workflowEngineServiceProvider = workflowEngineServiceProvider;
    }

    @Override
    protected void timerFired(FlowableEngineEntityEvent event) {
        Job job = event.getEntity() instanceof Job entity ? entity : null;
        if (job == null) {
            return;
        }
        String elementId = job.getElementId();
        if (elementId == null && job.getJobHandlerConfiguration() != null) {
            Matcher matcher = ACTIVITY_ID_PATTERN.matcher(job.getJobHandlerConfiguration());
            if (matcher.find()) {
                elementId = matcher.group(1);
            }
        }
        if (elementId == null || elementId.isBlank()) return;
        BpmnModel model = repositoryServiceProvider.getObject().getBpmnModel(event.getProcessDefinitionId());
        if (model == null) {
            return;
        }
        FlowElement element = model.getFlowElement(elementId);
        if (!(element instanceof BoundaryEvent boundaryEvent)) {
            return;
        }
        Integer boundaryEventType = extensionInteger(boundaryEvent,
                WorkflowProcessConstants.EXTENSION_BOUNDARY_EVENT_TYPE);
        if (boundaryEventType == null
                || (boundaryEventType != WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT
                && boundaryEventType != WorkflowProcessConstants.BOUNDARY_EVENT_DELAY_TIMER_TIMEOUT
                && boundaryEventType != WorkflowProcessConstants.BOUNDARY_EVENT_CHILD_PROCESS_TIMEOUT)) {
            return;
        }
        String taskDefinitionKey = boundaryEvent.getAttachedToRefId();
        if (boundaryEventType == WorkflowProcessConstants.BOUNDARY_EVENT_DELAY_TIMER_TIMEOUT) {
            triggerWaitingExecution(event.getProcessInstanceId(), taskDefinitionKey);
            return;
        }
        if (boundaryEventType == WorkflowProcessConstants.BOUNDARY_EVENT_CHILD_PROCESS_TIMEOUT) {
            terminateChildProcesses(event.getProcessInstanceId(), taskDefinitionKey);
            return;
        }
        String handlerTypeText = extensionValue(boundaryEvent,
                WorkflowProcessConstants.EXTENSION_TIMEOUT_HANDLER_TYPE);
        if (handlerTypeText == null) {
            return;
        }
        int handlerType;
        try {
            handlerType = Integer.parseInt(handlerTypeText);
        } catch (NumberFormatException exception) {
            return;
        }
        List<Task> tasks = taskServiceProvider.getObject().createTaskQuery()
                .processInstanceId(event.getProcessInstanceId())
                .taskDefinitionKey(taskDefinitionKey)
                .active()
                .list();
        if (tasks.isEmpty()) {
            return;
        }
        for (Task task : tasks) {
            // 清理/暂停流程与定时器回调可能并发到达；挂起任务不能写评论或完成变量。
            if (task.isSuspended()) {
                continue;
            }
            if (handlerType == WorkflowProcessConstants.TIMEOUT_HANDLER_REMINDER) {
                taskServiceProvider.getObject().addComment(task.getId(), task.getProcessInstanceId(),
                        "timeout-reminder", "审批超时提醒");
                log.warn("[timerFired][流程({}) 任务({}) 审批超时，当前仅记录提醒]",
                        event.getProcessInstanceId(), task.getId());
                continue;
            }
            try {
                boolean approved = handlerType == WorkflowProcessConstants.TIMEOUT_HANDLER_APPROVE;
                workflowEngineServiceProvider.getObject().completeAutomatically(task.getId(), approved,
                        approved ? "审批超时，系统自动通过" : "审批超时，系统自动拒绝");
            } catch (Exception exception) {
                log.error("[timerFired][流程({}) 任务({}) 超时自动处理失败]",
                        event.getProcessInstanceId(), task.getId(), exception);
            }
        }
    }

    private void triggerWaitingExecution(String processInstanceId, String activityId) {
        if (activityId == null || activityId.isBlank()) return;
        runtimeServiceProvider.getObject().createExecutionQuery()
                .processInstanceId(processInstanceId).activityId(activityId).list()
                .forEach(execution -> runtimeServiceProvider.getObject().trigger(execution.getId()));
    }

    private void terminateChildProcesses(String processInstanceId, String activityId) {
        if (activityId == null || activityId.isBlank()) return;
        Set<String> superExecutionIds = runtimeServiceProvider.getObject().createExecutionQuery()
                .processInstanceId(processInstanceId).activityId(activityId).list().stream()
                .map(Execution::getId).collect(java.util.stream.Collectors.toSet());
        List<ProcessInstance> children = runtimeServiceProvider.getObject().createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).list();
        for (ProcessInstance child : children) {
            if (superExecutionIds.isEmpty() || superExecutionIds.contains(child.getSuperExecutionId())) {
                // 子流程可能已被边界事件挂起；删除时禁止取消监听器再次向挂起任务补写变量。
                WorkflowProcessCleanupContext.enter();
                try {
                    runtimeServiceProvider.getObject().deleteProcessInstance(child.getId(), "子流程超时，系统自动结束");
                } finally {
                    WorkflowProcessCleanupContext.exit();
                }
            }
        }
    }

    private String extensionValue(FlowElement element, String name) {
        if (element.getExtensionElements() == null) {
            return null;
        }
        List<ExtensionElement> values = element.getExtensionElements().get(name);
        if (values == null || values.isEmpty() || values.get(0).getElementText() == null) {
            return null;
        }
        return values.get(0).getElementText().trim();
    }

    private Integer extensionInteger(FlowElement element, String name) {
        String value = extensionValue(element, name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

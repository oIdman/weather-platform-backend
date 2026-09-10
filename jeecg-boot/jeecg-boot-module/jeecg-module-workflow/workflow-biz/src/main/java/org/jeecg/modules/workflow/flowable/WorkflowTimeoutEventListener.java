package org.jeecg.modules.workflow.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 监听 Flowable TIMER_FIRED，处理简易设计器“审批超时”边界事件。
 */
@Slf4j
@Component
public class WorkflowTimeoutEventListener extends AbstractFlowableEngineEventListener {

    private final ObjectProvider<RepositoryService> repositoryServiceProvider;
    private final ObjectProvider<TaskService> taskServiceProvider;
    private final ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider;

    public WorkflowTimeoutEventListener(ObjectProvider<RepositoryService> repositoryServiceProvider,
                                        ObjectProvider<TaskService> taskServiceProvider,
                                        ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider) {
        super(Set.of(FlowableEngineEventType.TIMER_FIRED));
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.taskServiceProvider = taskServiceProvider;
        this.workflowEngineServiceProvider = workflowEngineServiceProvider;
    }

    @Override
    protected void timerFired(FlowableEngineEntityEvent event) {
        Job job = event.getEntity() instanceof Job entity ? entity : null;
        if (job == null || job.getElementId() == null) {
            return;
        }
        BpmnModel model = repositoryServiceProvider.getObject().getBpmnModel(event.getProcessDefinitionId());
        if (model == null) {
            return;
        }
        FlowElement element = model.getFlowElement(job.getElementId());
        if (!(element instanceof BoundaryEvent boundaryEvent)) {
            return;
        }
        Integer boundaryEventType = extensionInteger(boundaryEvent,
                WorkflowProcessConstants.EXTENSION_BOUNDARY_EVENT_TYPE);
        if (boundaryEventType == null
                || boundaryEventType != WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT) {
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
        String taskDefinitionKey = boundaryEvent.getAttachedToRefId();
        List<Task> tasks = taskServiceProvider.getObject().createTaskQuery()
                .processInstanceId(event.getProcessInstanceId())
                .taskDefinitionKey(taskDefinitionKey)
                .active()
                .list();
        if (tasks.isEmpty()) {
            return;
        }
        for (Task task : tasks) {
            if (handlerType == WorkflowProcessConstants.TIMEOUT_HANDLER_REMINDER) {
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

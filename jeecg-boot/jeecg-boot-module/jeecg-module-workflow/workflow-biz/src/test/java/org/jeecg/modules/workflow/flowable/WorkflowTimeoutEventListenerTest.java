package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowTimeoutEventListenerTest {

    @Test
    void automaticallyApprovesActiveTasksOnUserTaskTimeout() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        TaskService taskService = mock(TaskService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        Job job = mock(Job.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        Task task = task("task-1", "process-1", "approve");
        BoundaryEvent boundary = boundary("timeout-boundary", "approve",
                WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT,
                WorkflowProcessConstants.TIMEOUT_HANDLER_APPROVE);

        when(event.getEntity()).thenReturn(job);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(job.getElementId()).thenReturn("timeout-boundary");
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("approve")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(task.isSuspended()).thenReturn(false);

        WorkflowTimeoutEventListener listener = listener(taskService, workflowService,
                model(boundary), mock(RuntimeService.class));
        listener.timerFired(event);

        verify(workflowService).completeAutomatically("task-1", true, "审批超时，系统自动通过");
    }

    @Test
    void recordsReminderWithoutCompletingTask() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        TaskService taskService = mock(TaskService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        Job job = mock(Job.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        Task task = task("task-2", "process-1", "approve");
        BoundaryEvent boundary = boundary("timeout-boundary", "approve",
                WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT,
                WorkflowProcessConstants.TIMEOUT_HANDLER_REMINDER);

        when(event.getEntity()).thenReturn(job);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(job.getElementId()).thenReturn("timeout-boundary");
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("approve")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(task.isSuspended()).thenReturn(false);

        WorkflowTimeoutEventListener listener = listener(taskService, workflowService,
                model(boundary), mock(RuntimeService.class));
        listener.timerFired(event);

        verify(taskService).addComment("task-2", "process-1", "timeout-reminder", "审批超时提醒");
        org.mockito.Mockito.verifyNoInteractions(workflowService);
    }

    @Test
    void automaticallyRejectsActiveTasksOnUserTaskTimeout() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        TaskService taskService = mock(TaskService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        Job job = mock(Job.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        Task task = task("task-3", "process-1", "approve");
        BoundaryEvent boundary = boundary("timeout-boundary", "approve",
                WorkflowProcessConstants.BOUNDARY_EVENT_USER_TASK_TIMEOUT,
                WorkflowProcessConstants.TIMEOUT_HANDLER_REJECT);

        when(event.getEntity()).thenReturn(job);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(job.getElementId()).thenReturn("timeout-boundary");
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("approve")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(task.isSuspended()).thenReturn(false);

        WorkflowTimeoutEventListener listener = listener(taskService, workflowService,
                model(boundary), mock(RuntimeService.class));
        listener.timerFired(event);

        verify(workflowService).completeAutomatically("task-3", false, "审批超时，系统自动拒绝");
    }

    @Test
    void wakesWaitingExecutionOnDelayTimerBoundary() {
        TaskService taskService = mock(TaskService.class);
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ExecutionQuery executionQuery = mock(ExecutionQuery.class);
        Execution execution = mock(Execution.class);
        Job job = mock(Job.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        BoundaryEvent boundary = boundary("delay-boundary", "receive",
                WorkflowProcessConstants.BOUNDARY_EVENT_DELAY_TIMER_TIMEOUT, null);

        when(event.getEntity()).thenReturn(job);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(job.getElementId()).thenReturn("delay-boundary");
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId("process-1")).thenReturn(executionQuery);
        when(executionQuery.activityId("receive")).thenReturn(executionQuery);
        when(executionQuery.list()).thenReturn(List.of(execution));
        when(execution.getId()).thenReturn("execution-1");

        WorkflowTimeoutEventListener listener = listener(taskService, workflowService,
                model(boundary), runtimeService);
        listener.timerFired(event);

        verify(runtimeService).trigger("execution-1");
    }

    @Test
    void terminatesChildrenOnChildProcessTimeout() {
        TaskService taskService = mock(TaskService.class);
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        ExecutionQuery executionQuery = mock(ExecutionQuery.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        Execution parentExecution = mock(Execution.class);
        ProcessInstance child = mock(ProcessInstance.class);
        Job job = mock(Job.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        BoundaryEvent boundary = boundary("child-timeout-boundary", "call-child",
                WorkflowProcessConstants.BOUNDARY_EVENT_CHILD_PROCESS_TIMEOUT, null);

        when(event.getEntity()).thenReturn(job);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("parent-process");
        when(job.getElementId()).thenReturn("child-timeout-boundary");
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId("parent-process")).thenReturn(executionQuery);
        when(executionQuery.activityId("call-child")).thenReturn(executionQuery);
        when(executionQuery.list()).thenReturn(List.of(parentExecution));
        when(parentExecution.getId()).thenReturn("parent-execution");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.superProcessInstanceId("parent-process")).thenReturn(processQuery);
        when(processQuery.list()).thenReturn(List.of(child));
        when(child.getId()).thenReturn("child-process");
        when(child.getSuperExecutionId()).thenReturn("parent-execution");

        WorkflowTimeoutEventListener listener = listener(taskService, workflowService,
                model(boundary), runtimeService);
        listener.timerFired(event);

        verify(runtimeService).deleteProcessInstance("child-process", "子流程超时，系统自动结束");
    }

    private WorkflowTimeoutEventListener listener(TaskService taskService,
                                                   WorkflowEngineService workflowService,
                                                   BpmnModel model,
                                                   RuntimeService runtimeService) {
        RepositoryService repositoryService = mock(RepositoryService.class);
        when(repositoryService.getBpmnModel("definition-1")).thenReturn(model);
        return new WorkflowTimeoutEventListener(provider(repositoryService), provider(taskService),
                provider(runtimeService), provider(workflowService));
    }

    private BoundaryEvent boundary(String id, String attachedTaskId, int type, Integer handlerType) {
        BoundaryEvent event = new BoundaryEvent();
        event.setId(id);
        event.setAttachedToRefId(attachedTaskId);
        ExtensionElement eventType = extension(WorkflowProcessConstants.EXTENSION_BOUNDARY_EVENT_TYPE,
                String.valueOf(type));
        Map<String, List<ExtensionElement>> extensions = new java.util.LinkedHashMap<>();
        extensions.put(WorkflowProcessConstants.EXTENSION_BOUNDARY_EVENT_TYPE, List.of(eventType));
        if (handlerType != null) {
            extensions.put(WorkflowProcessConstants.EXTENSION_TIMEOUT_HANDLER_TYPE,
                    List.of(extension(WorkflowProcessConstants.EXTENSION_TIMEOUT_HANDLER_TYPE,
                            String.valueOf(handlerType))));
        }
        event.setExtensionElements(extensions);
        return event;
    }

    private BpmnModel model(BoundaryEvent boundary) {
        Process process = new Process();
        process.addFlowElement(boundary);
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return model;
    }

    private Task task(String id, String processInstanceId, String taskDefinitionKey) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(id);
        when(task.getProcessInstanceId()).thenReturn(processInstanceId);
        when(task.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
        return task;
    }

    private ExtensionElement extension(String name, String value) {
        ExtensionElement extension = new ExtensionElement();
        extension.setName(name);
        extension.setElementText(value);
        return extension;
    }

    private <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(value);
        return provider;
    }
}

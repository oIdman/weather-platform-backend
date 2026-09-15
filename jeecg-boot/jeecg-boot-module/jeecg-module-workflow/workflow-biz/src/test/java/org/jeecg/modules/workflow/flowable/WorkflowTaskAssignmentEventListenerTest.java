package org.jeecg.modules.workflow.flowable;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowTaskAssignmentEventListenerTest {

    @Test
    void marksActiveMultiInstanceTaskWhenCancellationHasNoMatchingHistoryExecution() {
        TaskService taskService = mock(TaskService.class);
        HistoryService historyService = mock(HistoryService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        HistoricActivityInstanceQuery historyQuery = mock(HistoricActivityInstanceQuery.class);
        Task task = mock(Task.class);
        FlowableActivityCancelledEvent event = mock(FlowableActivityCancelledEvent.class);

        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(historyQuery);
        when(historyQuery.executionId("multi-root")).thenReturn(historyQuery);
        when(historyQuery.list()).thenReturn(List.of());
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("approve")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(taskQuery.taskId("task-1")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getId()).thenReturn("task-1");
        when(taskService.getVariableLocal("task-1", WorkflowProcessConstants.TASK_VARIABLE_STATUS))
                .thenReturn(null);
        when(event.getExecutionId()).thenReturn("multi-root");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(event.getActivityId()).thenReturn("approve");

        WorkflowTaskAssignmentEventListener listener = new WorkflowTaskAssignmentEventListener(
                provider(taskService), provider(mock(RuntimeService.class)), provider(mock(RepositoryService.class)),
                provider(historyService), provider(mock(WorkflowEngineService.class)),
                provider(mock(WorkflowCandidateResolver.class)),
                mock(WorkflowCandidateIdentityAdapter.class));

        listener.activityCancelled(event);

        verify(taskService).setVariableLocal("task-1", WorkflowProcessConstants.TASK_VARIABLE_STATUS,
                WorkflowProcessConstants.STATUS_CANCELED);
        verify(taskService).setVariableLocal("task-1", WorkflowProcessConstants.TASK_VARIABLE_REASON,
                "因流程流转或多人审批结束，系统取消任务");
    }

    @Test
    void skipsCancellationVariablesForSuspendedTask() {
        TaskService taskService = mock(TaskService.class);
        HistoryService historyService = mock(HistoryService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        HistoricActivityInstanceQuery historyQuery = mock(HistoricActivityInstanceQuery.class);
        FlowableActivityCancelledEvent event = mock(FlowableActivityCancelledEvent.class);
        Task task = mock(Task.class);

        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(historyQuery);
        when(historyQuery.executionId("multi-root")).thenReturn(historyQuery);
        when(historyQuery.list()).thenReturn(List.of());
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("approve")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(taskQuery.taskId("task-1")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getId()).thenReturn("task-1");
        when(task.isSuspended()).thenReturn(true);
        when(event.getExecutionId()).thenReturn("multi-root");
        when(event.getProcessInstanceId()).thenReturn("process-1");
        when(event.getActivityId()).thenReturn("approve");

        WorkflowTaskAssignmentEventListener listener = new WorkflowTaskAssignmentEventListener(
                provider(taskService), provider(mock(RuntimeService.class)), provider(mock(RepositoryService.class)),
                provider(historyService), provider(mock(WorkflowEngineService.class)),
                provider(mock(WorkflowCandidateResolver.class)),
                mock(WorkflowCandidateIdentityAdapter.class));

        listener.activityCancelled(event);

        org.mockito.Mockito.verify(taskService, org.mockito.Mockito.never())
                .setVariableLocal(org.mockito.Mockito.anyString(), org.mockito.Mockito.anyString(), org.mockito.Mockito.any());
    }

    @Test
    void automaticallyApprovesConfiguredAutomaticApprovalTask() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        TaskService taskService = mock(TaskService.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        Task task = mock(Task.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        when(event.getEntity()).thenReturn(task);
        when(task.getId()).thenReturn("task-1");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(task.getTaskDefinitionKey()).thenReturn("auto-approve");
        when(task.isSuspended()).thenReturn(false);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-1")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(repositoryService.getBpmnModel("definition-1")).thenReturn(model("auto-approve", 2));

        WorkflowTaskAssignmentEventListener listener = listener(taskService, repositoryService, workflowService);
        listener.taskCreated(event);

        verify(workflowService).completeAutomatically("task-1", true, "节点配置为自动通过");
    }

    @Test
    void automaticallyRejectsConfiguredAutomaticRejectTask() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        TaskService taskService = mock(TaskService.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        Task task = mock(Task.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        when(event.getEntity()).thenReturn(task);
        when(task.getId()).thenReturn("task-2");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(task.getTaskDefinitionKey()).thenReturn("auto-reject");
        when(task.isSuspended()).thenReturn(false);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-2")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(repositoryService.getBpmnModel("definition-1")).thenReturn(model("auto-reject", 3));

        WorkflowTaskAssignmentEventListener listener = listener(taskService, repositoryService, workflowService);
        listener.taskCreated(event);

        verify(workflowService).completeAutomatically("task-2", false, "节点配置为自动拒绝");
    }

    @Test
    void assignsConfiguredEmptyAssigneeUsersForNativeBpmnTask() {
        WorkflowEngineService workflowService = mock(WorkflowEngineService.class);
        WorkflowCandidateResolver resolver = mock(WorkflowCandidateResolver.class);
        TaskService taskService = mock(TaskService.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        RepositoryService repositoryService = mock(RepositoryService.class);
        TaskQuery taskQuery = mock(TaskQuery.class);
        org.flowable.engine.runtime.ProcessInstanceQuery processQuery = mock(org.flowable.engine.runtime.ProcessInstanceQuery.class);
        Task task = mock(Task.class);
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        UserTask userTask = configuredEmptyFallbackTask("native-task", WorkflowProcessConstants.ASSIGN_EMPTY_USER);
        when(event.getEntity()).thenReturn(task);
        when(task.getId()).thenReturn("task-empty-user");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(task.getProcessInstanceId()).thenReturn("process-1");
        when(task.getTaskDefinitionKey()).thenReturn("native-task");
        when(task.getTenantId()).thenReturn("tenant-1");
        when(task.isSuspended()).thenReturn(false);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-empty-user")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(repositoryService.getBpmnModel("definition-1")).thenReturn(model(userTask));
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(mock(org.flowable.engine.runtime.ProcessInstance.class));
        when(resolver.resolveEmptyAssigneeCandidates(userTask, "definition-1", "tenant-1"))
                .thenReturn(Set.of("user-1", "user-2"));

        WorkflowTaskAssignmentEventListener listener = new WorkflowTaskAssignmentEventListener(
                provider(taskService), provider(runtimeService), provider(repositoryService),
                provider(mock(HistoryService.class)), provider(workflowService), provider(resolver),
                mock(WorkflowCandidateIdentityAdapter.class));
        listener.taskCreated(event);

        verify(taskService).addCandidateUser("task-empty-user", "user-1");
        verify(taskService).addCandidateUser("task-empty-user", "user-2");
    }

    private WorkflowTaskAssignmentEventListener listener(TaskService taskService,
                                                           RepositoryService repositoryService,
                                                           WorkflowEngineService workflowService) {
        return new WorkflowTaskAssignmentEventListener(
                provider(taskService), provider(mock(RuntimeService.class)), provider(repositoryService),
                provider(mock(HistoryService.class)), provider(workflowService),
                provider(mock(WorkflowCandidateResolver.class)), mock(WorkflowCandidateIdentityAdapter.class));
    }

    private BpmnModel model(String taskId, int approveType) {
        UserTask userTask = new UserTask();
        userTask.setId(taskId);
        ExtensionElement extension = new ExtensionElement();
        extension.setName(WorkflowProcessConstants.EXTENSION_APPROVE_TYPE);
        extension.setElementText(String.valueOf(approveType));
        userTask.setExtensionElements(java.util.Map.of(
                WorkflowProcessConstants.EXTENSION_APPROVE_TYPE, java.util.List.of(extension)));
        Process process = new Process();
        process.addFlowElement(userTask);
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return model;
    }

    private BpmnModel model(UserTask userTask) {
        Process process = new Process();
        process.addFlowElement(userTask);
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return model;
    }

    private UserTask configuredEmptyFallbackTask(String taskId, int handlerType) {
        UserTask userTask = new UserTask();
        userTask.setId(taskId);
        ExtensionElement handler = new ExtensionElement();
        handler.setName(WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE);
        handler.setElementText(String.valueOf(handlerType));
        userTask.setExtensionElements(java.util.Map.of(
                WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE, java.util.List.of(handler)));
        return userTask;
    }

    private <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(value);
        return provider;
    }
}

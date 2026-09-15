package org.jeecg.modules.workflow.service;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnNavigator;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnValidator;
import org.jeecg.modules.workflow.flowable.WorkflowCandidateResolver;
import org.jeecg.modules.workflow.service.impl.WorkflowEngineServiceImpl;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowCallbackSecurityTest {

    @Test
    void rejectsCallbackFromAnotherTenantBeforeLookingUpExecution() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.processInstanceTenantId("tenant-a")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(null);

        WorkflowEngineServiceImpl service = service(runtimeService, identity);

        assertThrows(JeecgBootException.class, () -> service.triggerCallback("process-1", "callback"));
        verify(runtimeService, never()).createExecutionQuery();
    }

    @Test
    void triggersOnlyAWaitingExecutionInTheCurrentTenant() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        ExecutionQuery executionQuery = mock(ExecutionQuery.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Execution execution = mock(Execution.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.processInstanceTenantId("tenant-a")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(processInstance);
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId("process-1")).thenReturn(executionQuery);
        when(executionQuery.activityId("callback")).thenReturn(executionQuery);
        when(executionQuery.singleResult()).thenReturn(execution);
        when(execution.getId()).thenReturn("execution-1");

        WorkflowEngineServiceImpl service = service(runtimeService, identity);
        service.triggerCallback(" process-1 ", " callback ");

        verify(runtimeService).trigger("execution-1");
    }

    private WorkflowEngineServiceImpl service(RuntimeService runtimeService, WorkflowIdentityAdapter identity) {
        return new WorkflowEngineServiceImpl(
                mock(RepositoryService.class), runtimeService, mock(TaskService.class), mock(HistoryService.class),
                identity, mock(WorkflowBpmnValidator.class), mock(ISysBaseAPI.class),
                mock(WorkflowModelService.class), mock(WorkflowCandidateResolver.class),
                new ObjectMapper(), mock(WorkflowBpmnNavigator.class));
    }
}

package org.jeecg.modules.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.eventsubscription.api.EventSubscription;
import org.flowable.eventsubscription.api.EventSubscriptionQuery;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowEventTriggerRequest;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnNavigator;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnValidator;
import org.jeecg.modules.workflow.flowable.WorkflowCandidateResolver;
import org.jeecg.modules.workflow.service.impl.WorkflowEngineServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowEventTriggerSecurityTest {

    @Test
    void triggersMessageSubscriptionWithTenantAndVariables() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        EventSubscriptionQuery subscriptionQuery = mock(EventSubscriptionQuery.class);
        EventSubscription subscription = mock(EventSubscription.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.processInstanceTenantId("tenant-a")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(processInstance);
        when(runtimeService.createEventSubscriptionQuery()).thenReturn(subscriptionQuery);
        when(subscriptionQuery.processInstanceId("process-1")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventType("message")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventName("reviewed")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.tenantId("tenant-a")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.list()).thenReturn(java.util.List.of(subscription));
        when(subscription.getExecutionId()).thenReturn("execution-1");

        WorkflowEngineServiceImpl service = service(runtimeService, identity);
        WorkflowEventTriggerRequest request = request("process-1", "message", "reviewed");
        request.setVariables(Map.of("decisionFlag", true));

        assertEquals(1, service.triggerEvent(request));
        verify(runtimeService).messageEventReceived("reviewed", "execution-1", Map.of("decisionFlag", true));
    }

    @Test
    void triggersSignalSubscriptionUsingSignalApi() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        EventSubscriptionQuery subscriptionQuery = mock(EventSubscriptionQuery.class);
        EventSubscription subscription = mock(EventSubscription.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.processInstanceTenantId("tenant-a")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(mock(ProcessInstance.class));
        when(runtimeService.createEventSubscriptionQuery()).thenReturn(subscriptionQuery);
        when(subscriptionQuery.processInstanceId("process-1")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventType("signal")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventName("approved")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.tenantId("tenant-a")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.list()).thenReturn(java.util.List.of(subscription));
        when(subscription.getExecutionId()).thenReturn("execution-2");

        WorkflowEngineServiceImpl service = service(runtimeService, identity);
        assertEquals(1, service.triggerEvent(request("process-1", "signal", "approved")));

        verify(runtimeService).signalEventReceived("approved", "execution-2", Map.of());
    }

    @Test
    void broadcastsSignalWithinCurrentTenantWhenInstanceIsOmitted() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        EventSubscriptionQuery subscriptionQuery = mock(EventSubscriptionQuery.class);
        EventSubscription subscription = mock(EventSubscription.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createEventSubscriptionQuery()).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventType("signal")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.eventName("approved")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.tenantId("tenant-a")).thenReturn(subscriptionQuery);
        when(subscriptionQuery.list()).thenReturn(java.util.List.of(subscription, mock(EventSubscription.class)));

        WorkflowEngineServiceImpl service = service(runtimeService, identity);
        WorkflowEventTriggerRequest request = request(null, "signal", "approved");
        request.setVariables(Map.of("decisionFlag", true));

        assertEquals(2, service.triggerEvent(request));
        verify(runtimeService).signalEventReceivedWithTenantId("approved", Map.of("decisionFlag", true), "tenant-a");
        verify(runtimeService, never()).createProcessInstanceQuery();
    }

    @Test
    void rejectsEventTriggerFromAnotherTenantBeforeQueryingSubscriptions() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        WorkflowIdentityAdapter identity = mock(WorkflowIdentityAdapter.class);
        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        when(identity.currentTenantId()).thenReturn("tenant-a");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processQuery);
        when(processQuery.processInstanceId("process-1")).thenReturn(processQuery);
        when(processQuery.processInstanceTenantId("tenant-a")).thenReturn(processQuery);
        when(processQuery.singleResult()).thenReturn(null);

        WorkflowEngineServiceImpl service = service(runtimeService, identity);
        assertThrows(JeecgBootException.class,
                () -> service.triggerEvent(request("process-1", "message", "reviewed")));
        verify(runtimeService, never()).createEventSubscriptionQuery();
    }

    private WorkflowEventTriggerRequest request(String processInstanceId, String eventType, String eventName) {
        WorkflowEventTriggerRequest request = new WorkflowEventTriggerRequest();
        request.setProcessInstanceId(processInstanceId);
        request.setEventType(eventType);
        request.setEventName(eventName);
        return request;
    }

    private WorkflowEngineServiceImpl service(RuntimeService runtimeService, WorkflowIdentityAdapter identity) {
        return new WorkflowEngineServiceImpl(
                mock(RepositoryService.class), runtimeService, mock(TaskService.class), mock(HistoryService.class),
                identity, mock(WorkflowBpmnValidator.class), mock(ISysBaseAPI.class),
                mock(WorkflowModelService.class), mock(WorkflowCandidateResolver.class),
                new ObjectMapper(), mock(WorkflowBpmnNavigator.class));
    }
}

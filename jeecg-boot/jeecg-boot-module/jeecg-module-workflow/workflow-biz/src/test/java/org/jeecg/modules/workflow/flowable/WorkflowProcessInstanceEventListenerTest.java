package org.jeecg.modules.workflow.flowable;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowProcessInstanceEventListenerTest {

    @Test
    void marksNaturallyCompletedRunningProcessAsApproved() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("process-1");
        when(instance.getProcessVariables()).thenReturn(Map.of(
                WorkflowProcessConstants.VARIABLE_STATUS, WorkflowProcessConstants.STATUS_RUNNING));
        FlowableEngineEntityEvent event = completedEvent(instance);

        new WorkflowProcessInstanceEventListener(provider(runtimeService)).onEvent(event);

        verify(runtimeService).setVariable("process-1", WorkflowProcessConstants.VARIABLE_STATUS,
                WorkflowProcessConstants.STATUS_APPROVED);
    }

    @Test
    void keepsRejectedStatusWhenProcessReachesEnd() {
        RuntimeService runtimeService = mock(RuntimeService.class);
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getProcessVariables()).thenReturn(Map.of(
                WorkflowProcessConstants.VARIABLE_STATUS, WorkflowProcessConstants.STATUS_REJECTED));
        FlowableEngineEntityEvent event = completedEvent(instance);

        new WorkflowProcessInstanceEventListener(provider(runtimeService)).onEvent(event);

        verify(runtimeService, never()).setVariable(instance.getId(), WorkflowProcessConstants.VARIABLE_STATUS,
                WorkflowProcessConstants.STATUS_APPROVED);
    }

    private FlowableEngineEntityEvent completedEvent(ProcessInstance instance) {
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        when(event.getType()).thenReturn(FlowableEngineEventType.PROCESS_COMPLETED);
        when(event.getEntity()).thenReturn(instance);
        return event;
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<RuntimeService> provider(RuntimeService runtimeService) {
        ObjectProvider<RuntimeService> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(runtimeService);
        return provider;
    }
}

package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.jeecg.modules.workflow.service.WorkflowProcessCopyService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowCopyNodeDelegateTest {

    @Test
    void persistsResolvedCopyUsersWithFlowableExecutionContext() {
        WorkflowCandidateResolver resolver = mock(WorkflowCandidateResolver.class);
        WorkflowProcessCopyService copyService = mock(WorkflowProcessCopyService.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        FlowElement element = mock(FlowElement.class);
        when(execution.getCurrentFlowElement()).thenReturn(element);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getProcessDefinitionId()).thenReturn("definition-1");
        when(execution.getTenantId()).thenReturn("tenant-1");
        when(element.getId()).thenReturn("copy-node");
        when(element.getName()).thenReturn("抄送");
        when(resolver.resolveCopyUsers(element, execution)).thenReturn(Set.of("user-1", "user-2"));

        new WorkflowCopyNodeDelegate(resolver, copyService).execute(execution);

        verify(copyService).copyFromNode(Set.of("user-1", "user-2"), "process-1", "definition-1",
                "copy-node", "抄送", "tenant-1", "抄送节点");
    }

    @Test
    void doesNotCreateCopyRowsWhenCandidateResolutionIsEmpty() {
        WorkflowCandidateResolver resolver = mock(WorkflowCandidateResolver.class);
        WorkflowProcessCopyService copyService = mock(WorkflowProcessCopyService.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        FlowElement element = mock(FlowElement.class);
        when(execution.getCurrentFlowElement()).thenReturn(element);
        when(resolver.resolveCopyUsers(element, execution)).thenReturn(Set.of());

        new WorkflowCopyNodeDelegate(resolver, copyService).execute(execution);

        verify(copyService, never()).copyFromNode(org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}

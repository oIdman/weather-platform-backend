package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.mapper.WorkflowUserGroupMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowCandidateResolverTest {

    private static final String FLOWABLE_NAMESPACE = "http://flowable.org/bpmn";

    @Test
    void stopsContinuousDepartmentLeaderTraversalWhenDepartmentTreeContainsCycle() {
        WorkflowCandidateIdentityAdapter identity = mock(WorkflowCandidateIdentityAdapter.class);
        WorkflowIdentityAdapter currentIdentity = mock(WorkflowIdentityAdapter.class);
        when(currentIdentity.currentTenantId()).thenReturn("0");
        when(identity.parentDepartmentIds(Set.of("dept-a"))).thenReturn(Set.of("dept-b"));
        when(identity.parentDepartmentIds(Set.of("dept-b"))).thenReturn(Set.of("dept-a"));
        when(identity.leaderUserIdsByDepartmentId("dept-a")).thenReturn(List.of("leader-a"));
        when(identity.leaderUserIdsByDepartmentId("dept-b")).thenReturn(List.of("leader-b"));
        when(identity.isActiveUser("leader-a")).thenReturn(true);
        when(identity.isActiveUser("leader-b")).thenReturn(true);

        WorkflowCandidateResolver resolver = resolver(identity, currentIdentity);
        UserTask task = task(23, "dept-a|10");

        assertEquals(Set.of("leader-a", "leader-b"), resolver.resolve(task, 23, null, Map.of()));
    }

    @Test
    void resolvesExactStartUserDepartmentLeaderLevelWithoutWalkingPastRequestedLevel() {
        WorkflowCandidateIdentityAdapter identity = mock(WorkflowCandidateIdentityAdapter.class);
        WorkflowIdentityAdapter currentIdentity = mock(WorkflowIdentityAdapter.class);
        when(currentIdentity.currentTenantId()).thenReturn("0");
        when(identity.primaryDepartmentIdByUserId("starter")).thenReturn("dept-a");
        when(identity.parentDepartmentIds(Set.of("dept-a"))).thenReturn(Set.of("dept-b"));
        when(identity.parentDepartmentIds(Set.of("dept-b"))).thenReturn(Set.of("dept-c"));
        when(identity.leaderUserIdsByDepartmentId("dept-b")).thenReturn(List.of("leader-b"));
        when(identity.isActiveUser("leader-b")).thenReturn(true);

        WorkflowCandidateResolver resolver = resolver(identity, currentIdentity);

        assertEquals(Set.of("leader-b"), resolver.resolve(task(37, "2"), 37, "starter", Map.of()));
    }

    @Test
    void resolvesRoleDepartmentPositionAndLiteralUserStrategiesWithActiveFiltering() {
        WorkflowCandidateIdentityAdapter identity = mock(WorkflowCandidateIdentityAdapter.class);
        WorkflowIdentityAdapter currentIdentity = mock(WorkflowIdentityAdapter.class);
        when(currentIdentity.currentTenantId()).thenReturn("0");
        when(identity.userIdsByRoleCodes(List.of("role-a"))).thenReturn(List.of("role-user", "disabled-user"));
        when(identity.userIdsByDepartmentIds(List.of("dept-a"))).thenReturn(List.of("dept-user"));
        when(identity.userIdsByPositionIds(List.of("post-a"))).thenReturn(List.of("post-user"));
        when(identity.isActiveUser("role-user")).thenReturn(true);
        when(identity.isActiveUser("disabled-user")).thenReturn(false);
        when(identity.isActiveUser("dept-user")).thenReturn(true);
        when(identity.isActiveUser("post-user")).thenReturn(true);
        when(identity.isActiveUser("literal-user")).thenReturn(true);

        WorkflowCandidateResolver resolver = resolver(identity, currentIdentity);

        assertEquals(Set.of("role-user"), resolver.resolve(task(10, "role-a"), 10, null, Map.of()));
        assertEquals(Set.of("dept-user"), resolver.resolve(task(20, "dept-a"), 20, null, Map.of()));
        assertEquals(Set.of("post-user"), resolver.resolve(task(22, "post-a"), 22, null, Map.of()));
        assertEquals(Set.of("literal-user"), resolver.resolve(task(30, "literal-user"), 30, null, Map.of()));
    }

    private WorkflowCandidateResolver resolver(WorkflowCandidateIdentityAdapter identity,
                                               WorkflowIdentityAdapter currentIdentity) {
        return new WorkflowCandidateResolver(identity, currentIdentity, mock(WorkflowUserGroupMapper.class),
                mock(ManagementService.class), mock(RepositoryService.class), new ObjectMapper());
    }

    private UserTask task(int strategy, String parameter) {
        UserTask task = new UserTask();
        task.setId("approve");
        Map<String, List<ExtensionElement>> extensions = new LinkedHashMap<>();
        extensions.put("candidateStrategy", List.of(extension("candidateStrategy", String.valueOf(strategy))));
        extensions.put("candidateParam", List.of(extension("candidateParam", parameter)));
        task.setExtensionElements(extensions);
        return task;
    }

    private ExtensionElement extension(String name, String value) {
        ExtensionElement extension = new ExtensionElement();
        extension.setName(name);
        extension.setElementText(value);
        extension.setNamespace(FLOWABLE_NAMESPACE);
        return extension;
    }
}

package org.jeecg.modules.workflow.starter;

import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 默认可替换的独立服务身份适配器。 */
@Component
@ConditionalOnMissingBean({WorkflowIdentityAdapter.class, WorkflowCandidateIdentityAdapter.class})
public class StandaloneWorkflowIdentityAdapter implements WorkflowIdentityAdapter, WorkflowCandidateIdentityAdapter {

    private final WorkflowStandaloneProperties properties;

    public StandaloneWorkflowIdentityAdapter(WorkflowStandaloneProperties properties) {
        this.properties = properties;
    }

    @Override
    public String currentUserId() {
        return textOrDefault(properties.getUserId(), "workflow-system");
    }

    @Override
    public String currentUsername() {
        return textOrDefault(properties.getUsername(), currentUserId());
    }

    @Override
    public List<String> currentRoleCodes() {
        return safeList(properties.getRoleCodes());
    }

    @Override
    public List<String> currentDepartmentIds() {
        return safeList(properties.getDepartmentIds());
    }

    @Override
    public String currentTenantId() {
        return textOrDefault(properties.getTenantId(), "0");
    }

    @Override
    public Collection<String> userIdsByRoleCodes(List<String> roleCodes) {
        return valuesForKeys(properties.getRoleUsers(), roleCodes);
    }

    @Override
    public Collection<String> userIdsByDepartmentIds(List<String> departmentIds) {
        return valuesForKeys(properties.getDepartmentUsers(), departmentIds);
    }

    @Override
    public Collection<String> leaderUserIdsByDepartmentId(String departmentId) {
        LinkedHashSet<String> userIds = new LinkedHashSet<>();
        if (properties.getDepartmentLeaderUsers() != null) {
            userIds.addAll(safeList(properties.getDepartmentLeaderUsers().get(departmentId)));
        }
        String userId = properties.getDepartmentLeaders() == null
                ? null : properties.getDepartmentLeaders().get(departmentId);
        if (userId != null && !userId.isBlank()) {
            userIds.add(userId.trim());
        }
        return List.copyOf(userIds);
    }

    @Override
    public Collection<String> userIdsByPositionIds(List<String> positionIds) {
        return valuesForKeys(properties.getPositionUsers(), positionIds);
    }

    @Override
    public Collection<String> userIdsByGroupIds(List<String> groupIds) {
        return valuesForKeys(properties.getUserGroupUsers(), groupIds);
    }

    @Override
    public List<String> departmentIdsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> departmentIds = new LinkedHashSet<>();
        if (userId.equals(properties.getUserId())) {
            departmentIds.addAll(safeList(properties.getDepartmentIds()));
        }
        if (properties.getDepartmentUsers() != null) {
            properties.getDepartmentUsers().forEach((departmentId, userIds) -> {
                if (safeList(userIds).contains(userId)) {
                    departmentIds.add(departmentId);
                }
            });
        }
        return List.copyOf(departmentIds);
    }

    @Override
    public String primaryDepartmentIdByUserId(String userId) {
        List<String> departmentIds = departmentIdsByUserId(userId);
        if (!departmentIds.isEmpty()) {
            return departmentIds.get(0);
        }
        // 未配置该用户的独立映射时，仅对启动器当前身份回退到默认部门，
        // 避免把其他候选人的部门误判成当前用户部门。
        return userId != null && userId.equals(properties.getUserId())
                ? safeList(properties.getDepartmentIds()).stream().findFirst().orElse(null)
                : null;
    }

    @Override
    public Set<String> parentDepartmentIds(Set<String> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty() || properties.getParentDepartments() == null) {
            return Collections.emptySet();
        }
        return departmentIds.stream()
                .map(properties.getParentDepartments()::get)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean isActiveUser(String userId) {
        return userId != null && !userId.isBlank();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values.stream()
                .filter(value -> value != null && !value.isBlank()).map(String::trim).distinct().toList();
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private Collection<String> valuesForKeys(Map<String, List<String>> values, List<String> keys) {
        if (values == null || keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return keys.stream()
                .map(values::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}

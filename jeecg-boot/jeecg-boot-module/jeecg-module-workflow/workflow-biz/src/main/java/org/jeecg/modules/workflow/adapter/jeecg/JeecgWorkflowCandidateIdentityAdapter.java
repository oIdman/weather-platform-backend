package org.jeecg.modules.workflow.adapter.jeecg;

import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** JeecgBoot 本地 system API 的候选人数据适配器。 */
@Component
public class JeecgWorkflowCandidateIdentityAdapter implements WorkflowCandidateIdentityAdapter {

    private final ISysBaseAPI sysBaseApi;

    public JeecgWorkflowCandidateIdentityAdapter(ISysBaseAPI sysBaseApi) {
        this.sysBaseApi = sysBaseApi;
    }

    @Override
    public Collection<String> userIdsByRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return emptyIfNull(sysBaseApi.queryUserIdsByRoleds(roleCodes));
    }

    @Override
    public Collection<String> userIdsByDepartmentIds(List<String> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return emptyIfNull(sysBaseApi.queryUserIdsByDeptIds(departmentIds));
    }

    @Override
    public Collection<String> leaderUserIdsByDepartmentId(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            return Collections.emptyList();
        }
        return emptyIfNull(sysBaseApi.getDeptHeadByDepId(departmentId));
    }

    @Override
    public Collection<String> userIdsByPositionIds(List<String> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return emptyIfNull(sysBaseApi.queryUserIdsByPositionIds(positionIds));
    }

    @Override
    public List<String> departmentIdsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        List<String> departmentIds = sysBaseApi.getDepartIdsByUserId(userId);
        return departmentIds == null ? Collections.emptyList() : departmentIds;
    }

    @Override
    public String primaryDepartmentIdByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        LoginUser user = sysBaseApi.getUserById(userId);
        if (user != null && user.getOrgId() != null && !user.getOrgId().isBlank()) {
            return user.getOrgId();
        }
        if (user != null && user.getOrgCode() != null && !user.getOrgCode().isBlank()) {
            String departmentId = sysBaseApi.getDepartIdsByOrgCode(user.getOrgCode());
            if (departmentId != null && !departmentId.isBlank()) {
                return departmentId;
            }
        }
        return departmentIdsByUserId(userId).stream().filter(Objects::nonNull)
                .filter(id -> !id.isBlank()).findFirst().orElse(null);
    }

    @Override
    public Set<String> parentDepartmentIds(Set<String> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> parentIds = sysBaseApi.getDepartParentIdsByDepIds(departmentIds);
        return parentIds == null ? Collections.emptySet() : parentIds;
    }

    @Override
    public boolean isActiveUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        LoginUser user = sysBaseApi.getUserById(userId);
        return user != null && Objects.equals(user.getStatus(), 1)
                && (user.getDelFlag() == null || Objects.equals(user.getDelFlag(), 0));
    }

    private Collection<String> emptyIfNull(Collection<String> values) {
        return values == null ? Collections.emptyList() : values;
    }
}

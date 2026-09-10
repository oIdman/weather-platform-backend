package org.jeecg.modules.workflow.adapter.jeecg;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class JeecgWorkflowIdentityAdapter implements WorkflowIdentityAdapter {

    private final ISysBaseAPI sysBaseApi;

    public JeecgWorkflowIdentityAdapter(ISysBaseAPI sysBaseApi) {
        this.sysBaseApi = sysBaseApi;
    }

    @Override
    public String currentUserId() {
        return currentUser().getId();
    }

    @Override
    public String currentUsername() {
        return currentUser().getUsername();
    }

    @Override
    public List<String> currentRoleCodes() {
        List<String> roles = sysBaseApi.getRolesByUsername(currentUsername());
        return roles == null ? Collections.emptyList() : roles;
    }

    @Override
    public List<String> currentDepartmentIds() {
        List<String> departmentIds = sysBaseApi.getDepartIdsByUsername(currentUsername());
        return departmentIds == null ? Collections.emptyList() : departmentIds;
    }

    @Override
    public String currentTenantId() {
        String tenantId = TenantContext.getTenant();
        return tenantId == null || tenantId.isBlank() ? "0" : tenantId;
    }

    private LoginUser currentUser() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (!(principal instanceof LoginUser loginUser)) {
            throw new JeecgBootException("当前用户未登录");
        }
        return loginUser;
    }
}

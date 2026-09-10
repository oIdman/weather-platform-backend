package org.jeecg.modules.workflow.adapter;

import java.util.List;

/**
 * 工作流核心与宿主系统身份体系之间的可移植边界。
 */
public interface WorkflowIdentityAdapter {
    String currentUserId();

    String currentUsername();

    List<String> currentRoleCodes();

    List<String> currentDepartmentIds();

    String currentTenantId();
}

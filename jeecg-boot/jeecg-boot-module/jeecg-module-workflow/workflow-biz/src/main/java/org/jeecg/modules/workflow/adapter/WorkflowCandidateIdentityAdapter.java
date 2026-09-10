package org.jeecg.modules.workflow.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 候选人计算所需的宿主组织与用户查询边界。
 *
 * <p>工作流运行时只依赖字符串编号，JeecgBoot 的用户、角色、部门和岗位实现放在适配器中，
 * 后续拆分独立工作流服务时可替换为远程实现。</p>
 */
public interface WorkflowCandidateIdentityAdapter {

    Collection<String> userIdsByRoleCodes(List<String> roleCodes);

    Collection<String> userIdsByDepartmentIds(List<String> departmentIds);

    Collection<String> leaderUserIdsByDepartmentId(String departmentId);

    Collection<String> userIdsByPositionIds(List<String> positionIds);

    List<String> departmentIdsByUserId(String userId);

    String primaryDepartmentIdByUserId(String userId);

    Set<String> parentDepartmentIds(Set<String> departmentIds);

    boolean isActiveUser(String userId);
}

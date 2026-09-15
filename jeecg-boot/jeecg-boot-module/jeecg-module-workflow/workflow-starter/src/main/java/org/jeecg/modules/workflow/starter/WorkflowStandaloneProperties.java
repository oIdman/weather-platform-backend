package org.jeecg.modules.workflow.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 独立工作流服务的最小身份上下文配置。
 *
 * <p>真实部署建议通过自定义 {@code WorkflowIdentityAdapter} 和
 * {@code WorkflowCandidateIdentityAdapter} Bean 接入业务用户中心；这里的默认值只用于
 * 启动器自带的基础运行和开发验证。</p>
 */
@Data
@ConfigurationProperties(prefix = "jeecg.workflow.standalone")
public class WorkflowStandaloneProperties {
    private String userId = "workflow-system";
    private String username = "workflow-system";
    private String tenantId = "0";
    private String displayName = "Workflow System";
    private List<String> roleCodes = new ArrayList<>();
    private List<String> departmentIds = new ArrayList<>();

    /** 可被工作流候选人解析使用的用户显示名，key 为用户 id。 */
    private Map<String, String> userNames = new LinkedHashMap<>();
    /** 角色编码到用户 id 的映射。 */
    private Map<String, List<String>> roleUsers = new LinkedHashMap<>();
    /** 部门 id 到用户 id 的映射。 */
    private Map<String, List<String>> departmentUsers = new LinkedHashMap<>();
    /** 岗位 id 到用户 id 的映射。 */
    private Map<String, List<String>> positionUsers = new LinkedHashMap<>();
    /** 用户组 id 到用户 id 的映射，供独立服务无需工作流用户组表时使用。 */
    private Map<String, List<String>> userGroupUsers = new LinkedHashMap<>();
    /** 部门 id 到负责人用户 id 的映射。 */
    private Map<String, String> departmentLeaders = new LinkedHashMap<>();
    /** 部门 id 到多个负责人用户 id 的映射，优先与单值映射合并使用。 */
    private Map<String, List<String>> departmentLeaderUsers = new LinkedHashMap<>();
    /** 部门 id 到父部门 id 的映射，用于连续多级部门负责人。 */
    private Map<String, String> parentDepartments = new LinkedHashMap<>();
    /** 部门 id 到展示名称的映射。 */
    private Map<String, String> departmentNames = new LinkedHashMap<>();
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.Map;

/**
 * 查询当前任务的下一审批节点。
 *
 * <p>字段命名与 Yudao 的 {@code BpmApprovalDetailReqVO} 保持一致，便于目标前端直接调用。</p>
 */
@Data
public class WorkflowApprovalDetailRequest {
    private String processDefinitionId;
    private Map<String, Object> processVariables;
    private String processVariablesStr;
    private String processInstanceId;
    private String activityId;
    private String taskId;

    @AssertTrue(message = "流程定义的编号和流程实例的编号不能同时为空")
    public boolean isValidProcessParam() {
        return hasText(processDefinitionId) || hasText(processInstanceId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

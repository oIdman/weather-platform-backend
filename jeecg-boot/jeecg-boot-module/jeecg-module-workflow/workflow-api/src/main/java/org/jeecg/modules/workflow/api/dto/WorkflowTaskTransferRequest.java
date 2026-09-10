package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowTaskTransferRequest {
    @NotBlank(message = "任务编号不能为空")
    private String id;

    @NotBlank(message = "新审批人不能为空")
    private String assigneeUserId;

    @NotBlank(message = "转办原因不能为空")
    private String reason;
}

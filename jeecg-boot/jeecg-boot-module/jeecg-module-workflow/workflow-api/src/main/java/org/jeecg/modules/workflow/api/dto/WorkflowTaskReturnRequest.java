package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowTaskReturnRequest {
    @NotBlank(message = "任务编号不能为空")
    private String id;

    @NotBlank(message = "退回节点不能为空")
    private String targetTaskDefinitionKey;

    @NotBlank(message = "退回原因不能为空")
    private String reason;
}

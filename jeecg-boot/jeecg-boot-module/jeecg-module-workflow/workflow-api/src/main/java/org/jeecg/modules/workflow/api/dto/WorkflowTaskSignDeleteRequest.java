package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowTaskSignDeleteRequest {
    @NotBlank(message = "被减签任务编号不能为空")
    private String id;

    @NotBlank(message = "减签原因不能为空")
    private String reason;
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowTaskDelegateRequest {
    @NotBlank(message = "任务编号不能为空")
    private String id;

    @NotBlank(message = "被委派人不能为空")
    private String delegateUserId;

    @NotBlank(message = "委派原因不能为空")
    private String reason;
}

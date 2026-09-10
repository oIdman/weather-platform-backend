package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowCancelRequest {
    @NotBlank
    private String id;
    @NotBlank
    private String reason;
}

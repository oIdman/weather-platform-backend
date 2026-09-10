package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowProcessExpressionSaveRequest {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Integer status;

    @NotBlank
    private String expression;
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowCategorySaveRequest {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    @NotNull
    private Integer status;

    @NotNull
    private Integer sort;
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowProcessListenerSaveRequest {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotNull
    private Integer status;

    @NotBlank
    private String event;

    @NotBlank
    private String valueType;

    @NotBlank
    private String value;
}

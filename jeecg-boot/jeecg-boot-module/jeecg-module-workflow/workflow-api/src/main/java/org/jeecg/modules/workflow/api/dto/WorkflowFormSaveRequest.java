package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowFormSaveRequest {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Integer status;

    @NotBlank
    private String conf;

    @NotNull
    @Size(min = 1)
    private List<String> fields;

    private String remark;
}

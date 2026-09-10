package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowUserGroupSaveRequest {
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private List<String> userIds;

    @NotNull
    private Integer status;
}

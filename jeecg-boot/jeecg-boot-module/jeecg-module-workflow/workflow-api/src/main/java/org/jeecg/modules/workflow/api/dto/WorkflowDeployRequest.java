package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowDeployRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String resourceName;
    @NotBlank
    private String bpmnXml;
    private String category;
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowTaskActionRequest {
    @NotBlank
    private String id;
    private String reason;
    private String signPicUrl;
    private List<String> attachments;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, List<String>> nextAssignees = new HashMap<>();
}

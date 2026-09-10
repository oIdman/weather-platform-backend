package org.jeecg.modules.workflow.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowStartRequest {
    private String processDefinitionId;
    private String processDefinitionKey;
    private String businessKey;
    private String name;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, List<String>> startUserSelectAssignees = new HashMap<>();
}

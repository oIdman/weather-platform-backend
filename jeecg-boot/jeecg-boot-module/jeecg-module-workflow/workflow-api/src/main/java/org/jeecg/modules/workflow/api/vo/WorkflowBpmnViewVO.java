package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowBpmnViewVO {
    private String bpmnXml;
    private List<String> currentActivityIds;
    private List<String> completedActivityIds;
}

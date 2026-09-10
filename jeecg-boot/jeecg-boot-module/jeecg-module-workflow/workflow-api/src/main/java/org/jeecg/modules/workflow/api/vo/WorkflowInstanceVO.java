package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowInstanceVO {
    private String id;
    private String name;
    private String businessKey;
    private String processDefinitionId;
    private String processDefinitionName;
    private String category;
    private String categoryName;
    private String startUserId;
    private String startUserName;
    private Date startTime;
    private Date endTime;
    private String status;
    private Integer resultStatus;
    private String reason;
    private List<String> currentTasks;
    private Map<String, Object> formVariables;
    private List<WorkflowTaskVO> tasks;
}

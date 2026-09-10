package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowTaskVO {
    private String id;
    private String name;
    private String taskDefinitionKey;
    private String processInstanceId;
    private String processDefinitionId;
    private String processInstanceName;
    private String processInstanceStatus;
    private String businessKey;
    private String category;
    private String categoryName;
    private String startUserId;
    private String startUserName;
    private String assignee;
    private String assigneeName;
    private String owner;
    private String ownerName;
    private String parentTaskId;
    private Date createTime;
    private Date endTime;
    private Long durationInMillis;
    private String status;
    private Integer resultStatus;
    private String reason;
    private String signPicUrl;
    private List<String> attachments;
    private List<WorkflowTaskVO> children;
}

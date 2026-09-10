package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WorkflowProcessCopyVO {
    private Long id;
    private String userId;
    private String userName;
    private String startUserId;
    private String startUserName;
    private String processInstanceId;
    private String processInstanceName;
    private Date processInstanceStartTime;
    private String processDefinitionId;
    private String category;
    private String categoryName;
    private String activityId;
    private String activityName;
    private String taskId;
    private String reason;
    private Date createTime;
}

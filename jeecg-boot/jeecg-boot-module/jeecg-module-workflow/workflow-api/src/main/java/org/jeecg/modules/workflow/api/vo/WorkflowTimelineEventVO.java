package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

/**
 * 流程实例中非审批任务的历史运行事件，例如网关、子流程和边界事件。
 */
@Data
public class WorkflowTimelineEventVO {
    private String id;
    private String activityId;
    private String name;
    private String type;
    private String executionId;
    private String processInstanceId;
    private Date startTime;
    private Date endTime;
    private Long durationInMillis;
    private Integer status;
    /** 用户任务对应的历史任务 ID；非用户活动为空。 */
    private String taskId;
    /** 子流程用户任务的实际办理人。 */
    private String assignee;
    private String assigneeName;
    private Integer resultStatus;
    private String reason;
}

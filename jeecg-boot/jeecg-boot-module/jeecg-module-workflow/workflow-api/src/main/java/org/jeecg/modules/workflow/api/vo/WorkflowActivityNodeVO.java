package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 流程预测中的审批节点，字段与目标前端 ApprovalNodeInfo 对齐。
 */
@Data
public class WorkflowActivityNodeVO {
    private String id;
    private String name;
    private Integer nodeType;
    private Integer status;
    private Date startTime;
    private Date endTime;
    private List<WorkflowTaskVO> tasks;
    /** 是否为 Flowable 多实例审批节点。 */
    private Boolean multiInstance;
    /** 多实例节点的总实例数、已完成实例数和当前活动实例数。 */
    private Integer instanceCount;
    private Integer completedInstanceCount;
    private Integer activeInstanceCount;
    private Integer completionPercent;
    private Integer candidateStrategy;
    private List<WorkflowUserSimpleVO> candidateUsers;
    private String processInstanceId;
}

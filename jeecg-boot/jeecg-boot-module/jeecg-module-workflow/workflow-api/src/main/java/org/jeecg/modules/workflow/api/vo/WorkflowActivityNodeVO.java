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
    private Integer candidateStrategy;
    private List<WorkflowUserSimpleVO> candidateUsers;
    private String processInstanceId;
}

package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WorkflowProcessExpressionVO {
    private Long id;
    private String name;
    private Integer status;
    private String expression;
    private Date createTime;
    private Date updateTime;
}

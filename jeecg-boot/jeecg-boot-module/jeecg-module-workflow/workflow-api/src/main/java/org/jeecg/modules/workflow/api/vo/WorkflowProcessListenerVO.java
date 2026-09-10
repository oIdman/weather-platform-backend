package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WorkflowProcessListenerVO {
    private Long id;
    private String name;
    private String type;
    private Integer status;
    private String event;
    private String valueType;
    private String value;
    private Date createTime;
    private Date updateTime;
}

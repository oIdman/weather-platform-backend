package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WorkflowCategoryVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private Integer sort;
    private Date createTime;
    private Date updateTime;
}

package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowFormVO {
    private Long id;
    private String name;
    private Integer status;
    private String conf;
    private List<String> fields;
    private String remark;
    private Date createTime;
    private Date updateTime;
}

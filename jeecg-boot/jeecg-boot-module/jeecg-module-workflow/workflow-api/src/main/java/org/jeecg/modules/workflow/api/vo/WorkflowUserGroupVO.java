package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowUserGroupVO {
    private Long id;
    private String name;
    private String description;
    private List<String> userIds;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}

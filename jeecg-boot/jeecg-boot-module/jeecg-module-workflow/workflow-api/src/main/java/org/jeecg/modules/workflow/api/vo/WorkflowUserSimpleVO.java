package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

@Data
public class WorkflowUserSimpleVO {
    private String id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
}

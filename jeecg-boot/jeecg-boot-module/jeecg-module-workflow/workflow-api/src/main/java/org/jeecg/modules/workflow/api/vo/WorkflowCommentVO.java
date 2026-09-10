package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WorkflowCommentVO {
    private String id;
    private String taskId;
    private TaskInfo task;
    private String processInstanceId;
    private String type;
    private String message;
    private Date createTime;
    private UserInfo user;

    @Data
    public static class TaskInfo {
        private String id;
        private String name;
        private String taskDefinitionKey;
    }

    @Data
    public static class UserInfo {
        private String id;
        private String nickname;
        private String avatar;
    }
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Collection;

@Data
public class WorkflowTaskCopyRequest {
    @NotBlank(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "抄送用户不能为空")
    private Collection<String> copyUserIds;

    private String reason;
}

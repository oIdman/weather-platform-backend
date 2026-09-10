package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class WorkflowTaskSignCreateRequest {
    @NotBlank(message = "任务编号不能为空")
    private String id;

    @NotEmpty(message = "加签用户不能为空")
    private Set<String> userIds;

    @NotBlank(message = "加签类型不能为空")
    @Pattern(regexp = "before|after", message = "加签类型必须为 before 或 after")
    private String type;

    @NotBlank(message = "加签原因不能为空")
    private String reason;
}

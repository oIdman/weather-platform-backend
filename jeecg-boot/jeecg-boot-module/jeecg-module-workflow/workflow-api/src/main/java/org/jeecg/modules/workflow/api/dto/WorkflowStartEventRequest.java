package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 外部系统通过消息启动事件启动流程的请求。 */
@Data
public class WorkflowStartEventRequest {
    @NotBlank(message = "消息名称不能为空")
    @Size(max = 128, message = "消息名称不能超过 128 个字符")
    private String messageName;

    @Size(max = 128, message = "业务标识不能超过 128 个字符")
    private String businessKey;

    @Size(max = 128, message = "流程实例名称不能超过 128 个字符")
    private String processInstanceName;

    private Map<String, Object> variables = new LinkedHashMap<>();

    private Map<String, List<String>> startUserSelectAssignees = new LinkedHashMap<>();
}

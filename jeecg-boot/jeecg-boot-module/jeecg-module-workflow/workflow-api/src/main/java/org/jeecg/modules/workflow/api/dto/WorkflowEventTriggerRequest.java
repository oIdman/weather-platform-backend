package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/** 外部业务触发流程消息/信号边界事件的请求。 */
@Data
public class WorkflowEventTriggerRequest {
    /**
     * 消息事件必须指定流程实例；信号事件允许为空，表示向当前租户广播。
     */
    private String processInstanceId;

    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    @NotBlank(message = "事件名称不能为空")
    @Size(max = 128, message = "事件名称不能超过 128 个字符")
    private String eventName;

    private Map<String, Object> variables = new LinkedHashMap<>();
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowModelStateRequest {
    @NotBlank(message = "流程模型编号不能为空")
    private String id;

    @NotNull(message = "流程状态不能为空")
    private Integer state;
}

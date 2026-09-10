package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class WorkflowModelSimpleUpdateRequest {
    @NotBlank(message = "流程模型编号不能为空")
    private String id;

    @NotNull(message = "简易流程模型不能为空")
    private Map<String, Object> simpleModel;
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WorkflowModelSaveRequest {
    private String id;

    @NotBlank(message = "流程标识不能为空")
    private String key;

    @NotBlank(message = "流程名称不能为空")
    private String name;

    private String category;
    private String icon;
    private String description;
    private Integer type;
    private Integer formType;
    private Long formId;
    private String formCustomCreatePath;
    private String formCustomViewPath;
    private Boolean visible;
    private List<String> startUserIds;
    private List<String> startDeptIds;
    private List<String> managerUserIds;
    private Long sort;
    private Boolean allowCancelRunningProcess;
    private Boolean allowWithdrawTask;
    private Map<String, Object> processIdRule;
    private Integer autoApprovalType;
    private Map<String, Object> titleSetting;
    private Map<String, Object> summarySetting;
    private Map<String, Object> processBeforeTriggerSetting;
    private Map<String, Object> processAfterTriggerSetting;
    private Map<String, Object> taskBeforeTriggerSetting;
    private Map<String, Object> taskAfterTriggerSetting;
    private Map<String, Object> printTemplateSetting;
    private String bpmnXml;
    private Map<String, Object> simpleModel;
}

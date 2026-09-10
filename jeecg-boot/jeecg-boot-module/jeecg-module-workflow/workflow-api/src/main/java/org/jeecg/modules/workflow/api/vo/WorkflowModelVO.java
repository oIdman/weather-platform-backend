package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowModelVO {
    private String id;
    private String key;
    private String name;
    private String category;
    private String categoryName;
    private String icon;
    private String description;
    private Integer type;
    private Integer formType;
    private Long formId;
    private String formName;
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
    private Date createTime;
    private Date updateTime;
    private String bpmnXml;
    private Map<String, Object> simpleModel;
    private Integer status;
    private WorkflowModelDefinitionVO processDefinition;
}

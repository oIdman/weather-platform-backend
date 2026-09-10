package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowDefinitionVO {
    private String id;
    private String key;
    private String name;
    private Integer version;
    private String category;
    private String categoryName;
    private String deploymentId;
    private String resourceName;
    private boolean suspended;
    private Integer suspensionState;
    private Date deploymentTime;
    private Integer modelType;
    private String modelId;
    private Integer formType;
    private Long formId;
    private String formName;
    private String formConf;
    private List<String> formFields;
    private String formCustomCreatePath;
    private String formCustomViewPath;
    private String icon;
    private String description;
    private Boolean visible;
    private Long sort;
    private String bpmnXml;
    private String simpleModel;
}

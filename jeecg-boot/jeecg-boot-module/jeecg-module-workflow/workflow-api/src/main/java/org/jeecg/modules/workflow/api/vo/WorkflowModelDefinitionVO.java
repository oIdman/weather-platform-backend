package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkflowModelDefinitionVO {
    private String id;
    private String key;
    private String name;
    private Integer version;
    private Date deploymentTime;
    private Integer suspensionState;
    private Integer formType;
    private Long formId;
    private String formName;
    private String formConf;
    private String formCustomCreatePath;
    private String formCustomViewPath;
    private List<String> formFields;
}

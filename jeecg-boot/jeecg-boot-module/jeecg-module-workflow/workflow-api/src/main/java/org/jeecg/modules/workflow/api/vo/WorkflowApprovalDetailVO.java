package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowApprovalDetailVO {
    private Integer status;
    private List<WorkflowActivityNodeVO> activityNodes = Collections.emptyList();
    private Map<String, String> formFieldsPermission = Collections.emptyMap();
    private List<Integer> enabledButtons = Collections.emptyList();
    private Map<Integer, String> buttonDisplayNames = Collections.emptyMap();
    private Boolean signEnable = Boolean.FALSE;
    private Boolean reasonRequire = Boolean.FALSE;
    private WorkflowInstanceVO processInstance;
    private List<WorkflowTaskVO> tasks;
    private WorkflowTaskVO todoTask;
    private WorkflowDefinitionVO processDefinition;
    private WorkflowBpmnViewVO bpmnModelView;
}

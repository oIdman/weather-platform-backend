package org.jeecg.modules.workflow.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowPrintDataVO {
    private boolean printTemplateEnable;
    private String printTemplateHtml;
    private WorkflowInstanceVO processInstance;
    private List<WorkflowTaskVO> tasks;
}

package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.jeecg.modules.workflow.service.WorkflowProcessCopyService;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 简易设计器“抄送节点”的运行时处理。
 *
 * <p>与芋道 {@code BpmCopyTaskDelegate} 对齐：进入节点时按节点候选人配置计算抄送人，
 * 并把抄送记录写入流程抄送列表，不产生待办任务。</p>
 */
@Component("workflowCopyTaskDelegate")
public class WorkflowCopyNodeDelegate implements JavaDelegate {

    private final WorkflowCandidateResolver candidateResolver;
    private final WorkflowProcessCopyService processCopyService;

    public WorkflowCopyNodeDelegate(WorkflowCandidateResolver candidateResolver,
                                    WorkflowProcessCopyService processCopyService) {
        this.candidateResolver = candidateResolver;
        this.processCopyService = processCopyService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        FlowElement flowElement = execution.getCurrentFlowElement();
        if (flowElement == null) {
            return;
        }
        Set<String> userIds = candidateResolver.resolveCopyUsers(flowElement, execution);
        if (userIds.isEmpty()) {
            return;
        }
        processCopyService.copyFromNode(userIds, execution.getProcessInstanceId(),
                execution.getProcessDefinitionId(), flowElement.getId(), flowElement.getName(),
                execution.getTenantId(), "抄送节点");
    }
}

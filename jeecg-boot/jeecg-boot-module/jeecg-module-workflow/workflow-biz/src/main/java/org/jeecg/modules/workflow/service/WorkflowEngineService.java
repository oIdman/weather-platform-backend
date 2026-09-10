package org.jeecg.modules.workflow.service;

import org.jeecg.modules.workflow.api.dto.WorkflowCancelRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowApprovalDetailRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowDeployRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessInstancePageRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowStartRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskActionRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskDelegateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskReturnRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignCreateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignDeleteRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskTransferRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskPageRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowDefinitionVO;
import org.jeecg.modules.workflow.api.vo.WorkflowActivityNodeVO;
import org.jeecg.modules.workflow.api.vo.WorkflowApprovalDetailVO;
import org.jeecg.modules.workflow.api.vo.WorkflowBpmnViewVO;
import org.jeecg.modules.workflow.api.vo.WorkflowInstanceVO;
import org.jeecg.modules.workflow.api.vo.WorkflowPrintDataVO;
import org.jeecg.modules.workflow.api.vo.WorkflowTaskVO;

import java.util.List;

public interface WorkflowEngineService {
    WorkflowDefinitionVO deploy(WorkflowDeployRequest request);

    WorkflowPage<WorkflowDefinitionVO> definitionPage(int pageNo, int pageSize, String name);

    List<WorkflowDefinitionVO> definitionList(int suspensionState, boolean startableOnly);

    List<WorkflowDefinitionVO> simpleDefinitionList();

    WorkflowDefinitionVO getDefinition(String id, String key);

    String start(WorkflowStartRequest request);

    WorkflowPage<WorkflowInstanceVO> myProcessPage(WorkflowProcessInstancePageRequest request);

    WorkflowPage<WorkflowInstanceVO> managerProcessPage(WorkflowProcessInstancePageRequest request);

    WorkflowInstanceVO getProcessInstance(String id);

    WorkflowBpmnViewVO getBpmnModelView(String processInstanceId);

    WorkflowApprovalDetailVO getApprovalDetail(WorkflowApprovalDetailRequest request);

    List<WorkflowActivityNodeVO> getNextApprovalNodes(WorkflowApprovalDetailRequest request);

    WorkflowPrintDataVO getPrintData(String processInstanceId);

    void cancel(WorkflowCancelRequest request, boolean manager);

    WorkflowPage<WorkflowTaskVO> todoPage(WorkflowTaskPageRequest request);

    WorkflowPage<WorkflowTaskVO> donePage(WorkflowTaskPageRequest request);

    WorkflowPage<WorkflowTaskVO> managerTaskPage(WorkflowTaskPageRequest request);

    List<WorkflowTaskVO> taskList(String processInstanceId);

    void approve(WorkflowTaskActionRequest request);

    void reject(WorkflowTaskActionRequest request);

    /** Internal runtime hook used by automatic node assignment rules; it is not exposed as a web API. */
    void completeAutomatically(String taskId, boolean approved, String reason);

    List<WorkflowTaskVO> returnableTaskList(String taskId);

    void returnTask(WorkflowTaskReturnRequest request);

    void delegateTask(WorkflowTaskDelegateRequest request);

    void transferTask(WorkflowTaskTransferRequest request);

    void createSignTask(WorkflowTaskSignCreateRequest request);

    void deleteSignTask(WorkflowTaskSignDeleteRequest request);

    List<WorkflowTaskVO> childTaskList(String parentTaskId);

    void withdrawTask(String taskId);

    void triggerCallback(String processInstanceId, String taskDefineKey);

    void deleteHistoricProcessInstance(String processInstanceId);
}

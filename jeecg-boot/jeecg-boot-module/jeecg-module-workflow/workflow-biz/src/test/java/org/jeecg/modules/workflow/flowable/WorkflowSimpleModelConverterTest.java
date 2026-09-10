package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkflowSimpleModelConverterTest {

    private final WorkflowSimpleModelConverter converter = new WorkflowSimpleModelConverter();
    private final WorkflowBpmnValidator validator = new WorkflowBpmnValidator();

    @Test
    void convertsLinearApprovalToRuntimeResolvedRandomAssignee() {
        Map<String, Object> approval = Map.of(
                "id", "ManagerApprove",
                "name", "经理审批",
                "type", 11,
                "candidateStrategy", 30,
                "candidateParam", List.of("user-1", "user-2"),
                "assignEmptyHandler", Map.of("type", 3, "userIds", List.of("fallback-user")),
                "assignStartUserHandlerType", 2,
                "childNode", Map.of("id", "EndNode", "name", "结束", "type", 1));
        Map<String, Object> model = Map.of(
                "id", "StartUserNode",
                "name", "发起人",
                "type", 10,
                "childNode", approval);

        String xml = converter.convert("leaveApproval", "请假审批", model);
        assertEquals("leaveApproval", validator.validate(xml).getMainProcess().getId());
        BpmnModel bpmnModel = parse(xml);
        UserTask userTask = (UserTask) bpmnModel.getMainProcess().getFlowElement("ManagerApprove");
        assertEquals("${workflowCandidateResolver.resolveOne(execution)}", userTask.getAssignee());
        assertEquals(List.of(), userTask.getCandidateUsers());
        assertEquals("30", userTask.getExtensionElements().get("candidateStrategy").get(0).getElementText());
        assertEquals("user-1,user-2", userTask.getExtensionElements().get("candidateParam").get(0).getElementText());
        assertEquals("3", userTask.getExtensionElements().get("assignEmptyHandlerType").get(0).getElementText());
        assertEquals("fallback-user", userTask.getExtensionElements().get("assignEmptyUserIds").get(0).getElementText());
        assertEquals("2", userTask.getExtensionElements().get("assignStartUserHandlerType").get(0).getElementText());
    }

    @Test
    void convertsStartUserSelectedApproverToRuntimeExpression() {
        Map<String, Object> selectedApproval = Map.of(
                "id", "SelectedApprove",
                "name", "自选审批",
                "type", 11,
                "candidateStrategy", 35,
                "childNode", Map.of("id", "EndNode", "name", "结束", "type", 1));
        Map<String, Object> approval = Map.of(
                "id", "ManagerApprove",
                "name", "经理审批",
                "type", 11,
                "assignee", "manager",
                "childNode", selectedApproval);

        BpmnModel bpmnModel = parse(converter.convert("selectedApproval", "自选审批流程", approval));
        UserTask userTask = (UserTask) bpmnModel.getMainProcess().getFlowElement("SelectedApprove");

        assertEquals("${PROCESS_START_USER_SELECT_ASSIGNEES['SelectedApprove'][0]}", userTask.getAssignee());
        assertEquals("35", userTask.getExtensionElements().get("candidateStrategy").get(0).getElementText());
        StartEvent startEvent = (StartEvent) bpmnModel.getMainProcess().getFlowElement("StartEvent_1");
        WorkflowBpmnNavigator navigator = new WorkflowBpmnNavigator(null);
        assertEquals(List.of("ManagerApprove"), navigator.nextUserTasks(startEvent, bpmnModel, Map.of()).stream()
                .map(UserTask::getId).toList());
        assertEquals(List.of("ManagerApprove", "SelectedApprove"), navigator.reachableUserTasks(startEvent, bpmnModel, Map.of()).stream()
                .map(UserTask::getId).toList());
    }

    @Test
    void convertsConditionalBranchAndMarksDefaultFlow() {
        Map<String, Object> approve = Map.of(
                "id", "FinanceApprove",
                "name", "财务审批",
                "type", 11,
                "assignee", "finance-user",
                "childNode", Map.of("id", "BranchEnd", "name", "结束", "type", 1));
        Map<String, Object> branch = Map.of(
                "id", "AmountBranch",
                "name", "金额条件",
                "type", 51,
                "conditionNodes", List.of(
                        Map.of("name", "大额", "conditionSetting", Map.of("conditionExpression", "amount > 1000"), "childNode", approve),
                        Map.of("name", "其他", "conditionSetting", Map.of("defaultFlow", true), "childNode", Map.of("id", "DefaultEnd", "name", "结束", "type", 1))),
                "childNode", Map.of("id", "EndNode", "name", "结束", "type", 1));

        String xml = converter.convert("expenseApproval", "报销审批", branch);
        assertEquals("expenseApproval", validator.validate(xml).getMainProcess().getId());
        ExclusiveGateway gateway = (ExclusiveGateway) parse(xml).getMainProcess().getFlowElement("AmountBranch");
        assertNotNull(gateway.getDefaultFlow());
    }

    private BpmnModel parse(String xml) {
        return new BpmnXMLConverter().convertToBpmnModel(
                new BytesStreamSource(xml.getBytes(StandardCharsets.UTF_8)), false, false);
    }
}

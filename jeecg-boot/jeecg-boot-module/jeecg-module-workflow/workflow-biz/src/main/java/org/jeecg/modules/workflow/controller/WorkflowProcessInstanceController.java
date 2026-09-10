package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowCancelRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowApprovalDetailRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessInstancePageRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowStartRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowInstanceVO;
import org.jeecg.modules.workflow.api.vo.WorkflowActivityNodeVO;
import org.jeecg.modules.workflow.api.vo.WorkflowApprovalDetailVO;
import org.jeecg.modules.workflow.api.vo.WorkflowBpmnViewVO;
import org.jeecg.modules.workflow.api.vo.WorkflowPrintDataVO;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.jeecg.modules.workflow.service.WorkflowProcessCopyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作流 - 流程实例")
@Validated
@RestController
@RequestMapping("/bpm/process-instance")
public class WorkflowProcessInstanceController {

    private final WorkflowEngineService workflowService;
    private final WorkflowProcessCopyService copyService;

    public WorkflowProcessInstanceController(WorkflowEngineService workflowService,
                                             WorkflowProcessCopyService copyService) {
        this.workflowService = workflowService;
        this.copyService = copyService;
    }

    @Operation(summary = "我的申请分页")
    @RequiresPermissions(value = {"bpm:process-instance:query", "workflow:process:list"}, logical = Logical.OR)
    @GetMapping("/my-page")
    public Result<WorkflowPage<WorkflowInstanceVO>> myPage(@Valid WorkflowProcessInstancePageRequest request) {
        return Result.ok(workflowService.myProcessPage(request));
    }

    @Operation(summary = "流程实例管理分页")
    @RequiresPermissions(value = {"bpm:process-instance:manager-query", "workflow:process:manager"}, logical = Logical.OR)
    @GetMapping("/manager-page")
    public Result<WorkflowPage<WorkflowInstanceVO>> managerPage(@Valid WorkflowProcessInstancePageRequest request) {
        return Result.ok(workflowService.managerProcessPage(request));
    }

    @Operation(summary = "查询流程实例详情")
    @RequiresPermissions(value = {"bpm:process-instance:query", "bpm:process-instance:manager-query", "workflow:process:list", "workflow:process:manager"}, logical = Logical.OR)
    @GetMapping("/get")
    public Result<WorkflowInstanceVO> get(@RequestParam String id) {
        return Result.ok(workflowService.getProcessInstance(id));
    }

    @Operation(summary = "查询流程审批详情")
    @RequiresPermissions(value = {"bpm:process-instance:query", "bpm:process-instance:manager-query", "workflow:process:list", "workflow:process:manager"}, logical = Logical.OR)
    @GetMapping("/get-approval-detail")
    public Result<WorkflowApprovalDetailVO> approvalDetail(@Valid WorkflowApprovalDetailRequest request) {
        return Result.ok(workflowService.getApprovalDetail(request));
    }

    @Operation(summary = "查询当前任务的下一审批节点")
    @RequiresPermissions(value = {"bpm:task:update", "workflow:task:handle"}, logical = Logical.OR)
    @GetMapping("/get-next-approval-nodes")
    public Result<List<WorkflowActivityNodeVO>> getNextApprovalNodes(
            @Valid WorkflowApprovalDetailRequest request) {
        return Result.ok(workflowService.getNextApprovalNodes(request));
    }

    @Operation(summary = "查询流程实例 BPMN 模型视图")
    @RequiresPermissions(value = {"bpm:process-instance:query", "bpm:process-instance:manager-query", "workflow:process:list", "workflow:process:manager"}, logical = Logical.OR)
    @GetMapping("/get-bpmn-model-view")
    public Result<WorkflowBpmnViewVO> bpmnModelView(@RequestParam("id") String processInstanceId) {
        return Result.ok(workflowService.getBpmnModelView(processInstanceId));
    }

    @Operation(summary = "查询流程实例打印数据")
    @RequiresPermissions(value = {"bpm:process-instance:query", "bpm:process-instance:manager-query", "workflow:process:list", "workflow:process:manager"}, logical = Logical.OR)
    @GetMapping("/get-print-data")
    public Result<WorkflowPrintDataVO> printData(@RequestParam String processInstanceId) {
        return Result.ok(workflowService.getPrintData(processInstanceId));
    }

    @Operation(summary = "发起流程")
    @RequiresPermissions(value = {"bpm:process-instance:create", "workflow:process:start"}, logical = Logical.OR)
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody WorkflowStartRequest request) {
        return Result.OK("流程发起成功", workflowService.start(request));
    }

    @Operation(summary = "发起人取消流程")
    @RequiresPermissions(value = {"bpm:process-instance:cancel", "workflow:process:cancel"}, logical = Logical.OR)
    @DeleteMapping("/cancel-by-start-user")
    public Result<Boolean> cancelByStartUser(@Valid @RequestBody WorkflowCancelRequest request) {
        workflowService.cancel(request, false);
        return Result.ok(true);
    }

    @Operation(summary = "管理员取消流程")
    @RequiresPermissions(value = {"bpm:process-instance:cancel-by-admin", "workflow:process:manager"}, logical = Logical.OR)
    @DeleteMapping("/cancel-by-admin")
    public Result<Boolean> cancelByAdmin(@Valid @RequestBody WorkflowCancelRequest request) {
        workflowService.cancel(request, true);
        return Result.ok(true);
    }

    @Operation(summary = "删除已结束流程实例")
    @RequiresPermissions("bpm:process-instance:manager-query")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam String id) {
        workflowService.deleteHistoricProcessInstance(id);
        copyService.deleteByProcessInstanceId(id);
        return Result.ok(true);
    }
}

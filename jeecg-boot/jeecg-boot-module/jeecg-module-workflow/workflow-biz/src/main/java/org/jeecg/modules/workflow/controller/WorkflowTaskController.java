package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskActionRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskCopyRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskDelegateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskReturnRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignCreateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignDeleteRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskPageRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskTransferRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowTaskVO;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.jeecg.modules.workflow.service.WorkflowProcessCopyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作流 - 任务")
@Validated
@RestController
@RequestMapping("/bpm/task")
public class WorkflowTaskController {

    private final WorkflowEngineService workflowService;
    private final WorkflowProcessCopyService copyService;

    public WorkflowTaskController(WorkflowEngineService workflowService, WorkflowProcessCopyService copyService) {
        this.workflowService = workflowService;
        this.copyService = copyService;
    }

    @Operation(summary = "我的待办分页")
    @RequiresPermissions(value = {"bpm:task:query", "workflow:task:list"}, logical = Logical.OR)
    @GetMapping("/todo-page")
    public Result<WorkflowPage<WorkflowTaskVO>> todoPage(@Valid WorkflowTaskPageRequest request) {
        return Result.ok(workflowService.todoPage(request));
    }

    @Operation(summary = "我的已办分页")
    @RequiresPermissions(value = {"bpm:task:query", "workflow:task:list"}, logical = Logical.OR)
    @GetMapping("/done-page")
    public Result<WorkflowPage<WorkflowTaskVO>> donePage(@Valid WorkflowTaskPageRequest request) {
        return Result.ok(workflowService.donePage(request));
    }

    @Operation(summary = "任务管理分页")
    @RequiresPermissions("bpm:task:manager-query")
    @GetMapping("/manager-page")
    public Result<WorkflowPage<WorkflowTaskVO>> managerPage(@Valid WorkflowTaskPageRequest request) {
        return Result.ok(workflowService.managerTaskPage(request));
    }

    @Operation(summary = "流程实例任务历史")
    @RequiresPermissions(value = {"bpm:task:query", "workflow:task:list"}, logical = Logical.OR)
    @GetMapping("/list-by-process-instance-id")
    public Result<List<WorkflowTaskVO>> listByProcessInstanceId(@RequestParam String processInstanceId) {
        return Result.ok(workflowService.taskList(processInstanceId));
    }

    @Operation(summary = "同意任务")
    @RequiresPermissions(value = {"bpm:task:update", "workflow:task:handle"}, logical = Logical.OR)
    @PutMapping("/approve")
    public Result<Boolean> approve(@Valid @RequestBody WorkflowTaskActionRequest request) {
        workflowService.approve(request);
        return Result.ok(true);
    }

    @Operation(summary = "驳回任务")
    @RequiresPermissions(value = {"bpm:task:update", "workflow:task:handle"}, logical = Logical.OR)
    @PutMapping("/reject")
    public Result<Boolean> reject(@Valid @RequestBody WorkflowTaskActionRequest request) {
        workflowService.reject(request);
        return Result.ok(true);
    }

    @Operation(summary = "抄送任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/copy")
    public Result<Boolean> copy(@Valid @RequestBody WorkflowTaskCopyRequest request) {
        copyService.copy(request);
        return Result.ok(true);
    }

    @Operation(summary = "获取当前任务可退回的历史节点")
    @RequiresPermissions("bpm:task:update")
    @GetMapping("/list-by-return")
    public Result<List<WorkflowTaskVO>> listByReturn(@RequestParam("id") String id) {
        return Result.ok(workflowService.returnableTaskList(id));
    }

    @Operation(summary = "退回任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/return")
    public Result<Boolean> returnTask(@Valid @RequestBody WorkflowTaskReturnRequest request) {
        workflowService.returnTask(request);
        return Result.ok(true);
    }

    @Operation(summary = "委派任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/delegate")
    public Result<Boolean> delegate(@Valid @RequestBody WorkflowTaskDelegateRequest request) {
        workflowService.delegateTask(request);
        return Result.ok(true);
    }

    @Operation(summary = "转办任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/transfer")
    public Result<Boolean> transfer(@Valid @RequestBody WorkflowTaskTransferRequest request) {
        workflowService.transferTask(request);
        return Result.ok(true);
    }

    @Operation(summary = "创建加签任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/create-sign")
    public Result<Boolean> createSign(@Valid @RequestBody WorkflowTaskSignCreateRequest request) {
        workflowService.createSignTask(request);
        return Result.ok(true);
    }

    @Operation(summary = "删除加签任务")
    @RequiresPermissions("bpm:task:update")
    @DeleteMapping("/delete-sign")
    public Result<Boolean> deleteSign(@Valid @RequestBody WorkflowTaskSignDeleteRequest request) {
        workflowService.deleteSignTask(request);
        return Result.ok(true);
    }

    @Operation(summary = "获取加签子任务")
    @RequiresPermissions(value = {"bpm:task:query", "workflow:task:list"}, logical = Logical.OR)
    @GetMapping("/list-by-parent-task-id")
    public Result<List<WorkflowTaskVO>> listByParentTaskId(@RequestParam String parentTaskId) {
        return Result.ok(workflowService.childTaskList(parentTaskId));
    }

    @Operation(summary = "撤回已办任务")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/withdraw")
    public Result<Boolean> withdraw(@RequestParam String taskId) {
        workflowService.withdrawTask(taskId);
        return Result.ok(true);
    }

    @Operation(summary = "触发 HTTP 回调等待节点")
    @RequiresPermissions("bpm:task:update")
    @PutMapping("/trigger-callback")
    public Result<Boolean> triggerCallback(@RequestParam String processInstanceId,
                                           @RequestParam String taskDefineKey) {
        workflowService.triggerCallback(processInstanceId, taskDefineKey);
        return Result.ok(true);
    }
}

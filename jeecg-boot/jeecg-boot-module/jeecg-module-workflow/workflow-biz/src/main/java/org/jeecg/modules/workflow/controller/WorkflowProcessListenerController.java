package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessListenerSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowProcessListenerVO;
import org.jeecg.modules.workflow.service.WorkflowProcessListenerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作流 - 流程监听器")
@Validated
@RestController
@RequestMapping("/bpm/process-listener")
public class WorkflowProcessListenerController {
    private final WorkflowProcessListenerService service;

    public WorkflowProcessListenerController(WorkflowProcessListenerService service) {
        this.service = service;
    }

    @Operation(summary = "创建流程监听器")
    @RequiresPermissions("bpm:process-listener:create")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody WorkflowProcessListenerSaveRequest request) {
        return Result.OK("创建成功", service.create(request));
    }

    @Operation(summary = "更新流程监听器")
    @RequiresPermissions("bpm:process-listener:update")
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody WorkflowProcessListenerSaveRequest request) {
        service.update(request);
        return Result.ok(true);
    }

    @Operation(summary = "删除流程监听器")
    @RequiresPermissions("bpm:process-listener:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        service.delete(id);
        return Result.ok(true);
    }

    @Operation(summary = "流程监听器详情")
    @RequiresPermissions("bpm:process-listener:query")
    @GetMapping("/get")
    public Result<WorkflowProcessListenerVO> get(@RequestParam Long id) {
        return Result.ok(service.get(id));
    }

    @Operation(summary = "流程监听器分页")
    @RequiresPermissions("bpm:process-listener:query")
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowProcessListenerVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        return Result.ok(service.page(pageNo, pageSize, name, status, type));
    }
}

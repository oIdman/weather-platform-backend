package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowFormSaveRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowFormVO;
import org.jeecg.modules.workflow.service.WorkflowFormService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作流 - 流程表单")
@Validated
@RestController
@RequestMapping("/bpm/form")
public class WorkflowFormController {
    private final WorkflowFormService service;

    public WorkflowFormController(WorkflowFormService service) {
        this.service = service;
    }

    @Operation(summary = "创建流程表单")
    @RequiresPermissions("bpm:form:create")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody WorkflowFormSaveRequest request) {
        return Result.OK("创建成功", service.create(request));
    }

    @Operation(summary = "更新流程表单")
    @RequiresPermissions("bpm:form:update")
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody WorkflowFormSaveRequest request) {
        service.update(request);
        return Result.ok(true);
    }

    @Operation(summary = "删除流程表单")
    @RequiresPermissions("bpm:form:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        service.delete(id);
        return Result.ok(true);
    }

    @Operation(summary = "流程表单详情")
    @RequiresPermissions("bpm:form:query")
    @GetMapping("/get")
    public Result<WorkflowFormVO> get(@RequestParam Long id) {
        return Result.ok(service.get(id));
    }

    @Operation(summary = "流程表单分页")
    @RequiresPermissions("bpm:form:query")
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowFormVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.ok(service.page(pageNo, pageSize, name, status));
    }

    @Operation(summary = "启用的流程表单精简列表")
    @GetMapping("/simple-list")
    public Result<List<WorkflowFormVO>> simpleList() {
        return Result.ok(service.simpleList());
    }
}

package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowCategorySaveRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowCategoryVO;
import org.jeecg.modules.workflow.service.WorkflowCategoryService;
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

@Tag(name = "工作流 - 流程分类")
@Validated
@RestController
@RequestMapping("/bpm/category")
public class WorkflowCategoryController {
    private final WorkflowCategoryService service;

    public WorkflowCategoryController(WorkflowCategoryService service) {
        this.service = service;
    }

    @Operation(summary = "创建流程分类")
    @RequiresPermissions("bpm:category:create")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody WorkflowCategorySaveRequest request) {
        return Result.OK("创建成功", service.create(request));
    }

    @Operation(summary = "更新流程分类")
    @RequiresPermissions("bpm:category:update")
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody WorkflowCategorySaveRequest request) {
        service.update(request);
        return Result.ok(true);
    }

    @Operation(summary = "批量更新流程分类排序")
    @RequiresPermissions("bpm:category:update")
    @PutMapping("/update-sort-batch")
    public Result<Boolean> updateSortBatch(@RequestParam List<Long> ids) {
        service.updateSortBatch(ids);
        return Result.ok(true);
    }

    @Operation(summary = "删除流程分类")
    @RequiresPermissions("bpm:category:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        service.delete(id);
        return Result.ok(true);
    }

    @Operation(summary = "流程分类详情")
    @RequiresPermissions("bpm:category:query")
    @GetMapping("/get")
    public Result<WorkflowCategoryVO> get(@RequestParam Long id) {
        return Result.ok(service.get(id));
    }

    @Operation(summary = "流程分类分页")
    @RequiresPermissions("bpm:category:query")
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowCategoryVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.ok(service.page(pageNo, pageSize, name, status));
    }

    @Operation(summary = "启用的流程分类精简列表")
    @GetMapping("/simple-list")
    public Result<List<WorkflowCategoryVO>> simpleList() {
        return Result.ok(service.simpleList());
    }
}

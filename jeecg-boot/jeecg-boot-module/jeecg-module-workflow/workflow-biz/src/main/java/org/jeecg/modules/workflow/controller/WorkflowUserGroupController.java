package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowUserGroupSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowUserGroupVO;
import org.jeecg.modules.workflow.service.WorkflowUserGroupService;
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

@Tag(name = "工作流 - 用户组")
@Validated
@RestController
@RequestMapping("/bpm/user-group")
public class WorkflowUserGroupController {
    private final WorkflowUserGroupService service;

    public WorkflowUserGroupController(WorkflowUserGroupService service) {
        this.service = service;
    }

    @Operation(summary = "创建流程用户组")
    @RequiresPermissions("bpm:user-group:create")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody WorkflowUserGroupSaveRequest request) {
        return Result.OK("创建成功", service.create(request));
    }

    @Operation(summary = "更新流程用户组")
    @RequiresPermissions("bpm:user-group:update")
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody WorkflowUserGroupSaveRequest request) {
        service.update(request);
        return Result.ok(true);
    }

    @Operation(summary = "删除流程用户组")
    @RequiresPermissions("bpm:user-group:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long id) {
        service.delete(id);
        return Result.ok(true);
    }

    @Operation(summary = "流程用户组详情")
    @RequiresPermissions("bpm:user-group:query")
    @GetMapping("/get")
    public Result<WorkflowUserGroupVO> get(@RequestParam Long id) {
        return Result.ok(service.get(id));
    }

    @Operation(summary = "流程用户组分页")
    @RequiresPermissions("bpm:user-group:query")
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowUserGroupVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        return Result.ok(service.page(pageNo, pageSize, name, status));
    }

    @Operation(summary = "启用的流程用户组精简列表")
    @GetMapping("/simple-list")
    public Result<List<WorkflowUserGroupVO>> simpleList() {
        return Result.ok(service.simpleList());
    }
}

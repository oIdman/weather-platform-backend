package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowDeployRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowDefinitionVO;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作流 - 流程定义")
@Validated
@RestController
@RequestMapping("/bpm/process-definition")
public class WorkflowDefinitionController {

    private final WorkflowEngineService workflowService;

    public WorkflowDefinitionController(WorkflowEngineService workflowService) {
        this.workflowService = workflowService;
    }

    @Operation(summary = "流程定义分页")
    @RequiresPermissions(value = {"bpm:process-definition:query", "workflow:definition:list"}, logical = Logical.OR)
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowDefinitionVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        return Result.ok(workflowService.definitionPage(pageNo, pageSize, name));
    }

    @Operation(summary = "流程定义列表")
    @GetMapping("/list")
    public Result<List<WorkflowDefinitionVO>> list(
            @RequestParam(defaultValue = "1") Integer suspensionState) {
        return Result.ok(workflowService.definitionList(suspensionState, true));
    }

    @Operation(summary = "流程定义精简列表")
    @GetMapping("/simple-list")
    public Result<List<WorkflowDefinitionVO>> simpleList() {
        return Result.ok(workflowService.simpleDefinitionList());
    }

    @Operation(summary = "流程定义详情")
    @GetMapping("/get")
    public Result<WorkflowDefinitionVO> get(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String key) {
        return Result.ok(workflowService.getDefinition(id, key));
    }

    @Operation(summary = "校验并部署 BPMN 流程")
    @RequiresPermissions(value = {"bpm:model:deploy", "workflow:definition:deploy"}, logical = Logical.OR)
    @PostMapping("/deploy")
    public Result<WorkflowDefinitionVO> deploy(@Valid @RequestBody WorkflowDeployRequest request) {
        return Result.OK("流程部署成功", workflowService.deploy(request));
    }
}

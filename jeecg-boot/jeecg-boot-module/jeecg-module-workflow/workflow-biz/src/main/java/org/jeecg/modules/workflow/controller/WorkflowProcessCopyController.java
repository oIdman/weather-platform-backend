package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowProcessCopyVO;
import org.jeecg.modules.workflow.service.WorkflowProcessCopyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作流 - 流程抄送")
@Validated
@RestController
@RequestMapping("/bpm/process-instance/copy")
public class WorkflowProcessCopyController {
    private final WorkflowProcessCopyService copyService;

    public WorkflowProcessCopyController(WorkflowProcessCopyService copyService) {
        this.copyService = copyService;
    }

    @Operation(summary = "抄送我的分页")
    @RequiresPermissions("bpm:process-instance-cc:query")
    @GetMapping("/page")
    public Result<WorkflowPage<WorkflowProcessCopyVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String processInstanceName) {
        return Result.ok(copyService.page(pageNo, pageSize, processInstanceName));
    }
}

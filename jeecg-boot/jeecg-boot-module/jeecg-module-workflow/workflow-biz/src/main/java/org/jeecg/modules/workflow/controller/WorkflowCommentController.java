package org.jeecg.modules.workflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.workflow.api.dto.WorkflowCommentCreateRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowCommentVO;
import org.jeecg.modules.workflow.service.WorkflowCommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工作流 - 流程评论")
@Validated
@RestController
@RequestMapping("/bpm/comment")
public class WorkflowCommentController {
    private final WorkflowCommentService commentService;

    public WorkflowCommentController(WorkflowCommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "查询流程实例评论")
    @RequiresPermissions(value = {"bpm:task:query", "workflow:task:list"}, logical = Logical.OR)
    @GetMapping("/list-by-process-instance-id")
    public Result<List<WorkflowCommentVO>> list(@RequestParam String processInstanceId) {
        return Result.ok(commentService.list(processInstanceId));
    }

    @Operation(summary = "创建流程评论")
    @RequiresPermissions(value = {"bpm:task:update", "workflow:task:handle"}, logical = Logical.OR)
    @PostMapping("/create")
    public Result<Boolean> create(@Valid @RequestBody WorkflowCommentCreateRequest request) {
        commentService.create(request);
        return Result.ok(true);
    }
}

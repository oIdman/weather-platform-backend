package org.jeecg.modules.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.api.dto.WorkflowModelSaveRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowModelSimpleUpdateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowModelStateRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowModelVO;
import org.jeecg.modules.workflow.service.WorkflowModelService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Tag(name = "工作流 - 流程模型")
@Validated
@RestController
@RequestMapping("/bpm/model")
public class WorkflowModelController {
    private static final long MAX_IMPORT_BYTES = 1024 * 1024;

    private final WorkflowModelService modelService;
    private final ObjectMapper objectMapper;

    public WorkflowModelController(WorkflowModelService modelService, ObjectMapper objectMapper) {
        this.modelService = modelService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "流程模型列表")
    @GetMapping("/list")
    public Result<List<WorkflowModelVO>> list(@RequestParam(required = false) String name) {
        return Result.ok(modelService.list(name));
    }

    @Operation(summary = "流程模型详情")
    @RequiresPermissions("bpm:model:query")
    @GetMapping("/get")
    public Result<WorkflowModelVO> get(@RequestParam String id) {
        return Result.ok(modelService.get(id));
    }

    @Operation(summary = "导出流程模型")
    @RequiresPermissions("bpm:model:export")
    @GetMapping("/export")
    public Result<WorkflowModelSaveRequest> exportModel(@RequestParam String id) {
        return Result.ok(modelService.exportModel(id));
    }

    @Operation(summary = "创建流程模型")
    @RequiresPermissions("bpm:model:create")
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody WorkflowModelSaveRequest request) {
        return Result.OK("流程模型创建成功", modelService.create(request));
    }

    @Operation(summary = "导入流程模型")
    @RequiresPermissions("bpm:model:import")
    @PostMapping("/import")
    public Result<String> importModel(@RequestParam MultipartFile file,
                                      @RequestParam(required = false) String key,
                                      @RequestParam(required = false) String name) throws IOException {
        validateImportFile(file);
        WorkflowModelSaveRequest request;
        try {
            request = objectMapper.readValue(file.getBytes(), WorkflowModelSaveRequest.class);
        } catch (RuntimeException exception) {
            throw new JeecgBootException("流程模型导入文件不是有效的 JSON", exception);
        }
        if (key != null && !key.isBlank()) {
            request.setKey(key);
        }
        if (name != null && !name.isBlank()) {
            request.setName(name);
        }
        return Result.OK("流程模型导入成功", modelService.create(request));
    }

    @Operation(summary = "更新流程模型")
    @RequiresPermissions("bpm:model:update")
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody WorkflowModelSaveRequest request) {
        modelService.update(request);
        return Result.ok(true);
    }

    @Operation(summary = "批量更新流程模型排序")
    @RequiresPermissions("bpm:model:update")
    @PutMapping("/update-sort-batch")
    public Result<Boolean> updateSortBatch(@RequestParam List<String> ids) {
        modelService.updateSortBatch(ids);
        return Result.ok(true);
    }

    @Operation(summary = "更新 BPMN XML")
    @RequiresPermissions("bpm:model:update")
    @PutMapping("/update-bpmn")
    public Result<Boolean> updateBpmn(@RequestBody Map<String, String> request) {
        modelService.updateBpmn(request.get("id"), request.get("bpmnXml"));
        return Result.ok(true);
    }

    @Operation(summary = "获取简易流程模型")
    @GetMapping("/simple/get")
    public Result<Map<String, Object>> getSimple(@RequestParam String id) {
        return Result.ok(modelService.get(id).getSimpleModel());
    }

    @Operation(summary = "更新简易流程模型")
    @RequiresPermissions("bpm:model:update")
    @PostMapping("/simple/update")
    public Result<Boolean> updateSimple(@Valid @RequestBody WorkflowModelSimpleUpdateRequest request) {
        modelService.updateSimple(request.getId(), request.getSimpleModel());
        return Result.ok(true);
    }

    @Operation(summary = "发布流程模型")
    @RequiresPermissions("bpm:model:deploy")
    @PostMapping("/deploy")
    public Result<Boolean> deploy(@RequestParam String id) {
        modelService.deploy(id);
        return Result.ok(true);
    }

    @Operation(summary = "更新流程模型状态")
    @RequiresPermissions("bpm:model:update")
    @PutMapping("/update-state")
    public Result<Boolean> updateState(@Valid @RequestBody WorkflowModelStateRequest request) {
        modelService.updateState(request.getId(), request.getState());
        return Result.ok(true);
    }

    @Operation(summary = "删除流程模型")
    @RequiresPermissions("bpm:model:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam String id) {
        modelService.delete(id);
        return Result.ok(true);
    }

    @Operation(summary = "清理流程模型实例")
    @RequiresPermissions("bpm:model:clean")
    @DeleteMapping("/clean")
    public Result<Boolean> clean(@RequestParam String id) {
        modelService.clean(id);
        return Result.ok(true);
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new JeecgBootException("请选择流程模型 JSON 文件");
        }
        if (file.getSize() > MAX_IMPORT_BYTES) {
            throw new JeecgBootException("流程模型导入文件不能超过 1 MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new JeecgBootException("只允许导入 JSON 文件");
        }
    }
}

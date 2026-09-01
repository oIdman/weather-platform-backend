package org.jeecg.modules.project.execution.midcheck.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.execution.midcheck.entity.MrpRectificationTask;
import org.jeecg.modules.project.execution.midcheck.service.IMrpRectificationTaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 整改任务看板
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "整改任务看板")
@RestController
@RequestMapping("/project/execution/rectificationTask")
@Slf4j
public class MrpRectificationTaskController extends JeecgController<MrpRectificationTask, IMrpRectificationTaskService> {

    @Operation(summary = "整改任务分页列表查询", description = "可按项目/状态/责任人过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpRectificationTask>> queryPageList(MrpRectificationTask mrpRectificationTask,
                                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             HttpServletRequest req) {
        QueryWrapper<MrpRectificationTask> queryWrapper = QueryGenerator.initQueryWrapper(mrpRectificationTask, req.getParameterMap());
        queryWrapper.orderByAsc("status").orderByAsc("deadline");
        Page<MrpRectificationTask> page = new Page<>(pageNo, pageSize);
        IPage<MrpRectificationTask> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "整改任务添加", description = "整改任务添加（默认待处理）")
    @PostMapping(value = "/add")
    public Result<MrpRectificationTask> add(@RequestBody MrpRectificationTask mrpRectificationTask) {
        service.save(mrpRectificationTask);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "整改任务编辑", description = "整改任务编辑")
    @PutMapping(value = "/edit")
    public Result<MrpRectificationTask> edit(@RequestBody MrpRectificationTask mrpRectificationTask) {
        MrpRectificationTask byId = service.getById(mrpRectificationTask.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpRectificationTask);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "开始处理", description = "待处理 → 处理中")
    @PostMapping(value = "/start")
    public Result<MrpRectificationTask> start(@RequestParam(name = "id", required = true) String id) {
        service.start(id);
        return Result.ok("已开始处理！");
    }

    @Operation(summary = "完成整改", description = "处理中 → 已完成（记录处理说明）")
    @PostMapping(value = "/complete")
    public Result<MrpRectificationTask> complete(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String handleContent = body.get("handleContent");
        if (id == null) {
            return Result.error("参数不完整！");
        }
        service.complete(id, handleContent);
        return Result.ok("整改完成！");
    }

    @Operation(summary = "关闭任务", description = "任意状态 → 已关闭")
    @PostMapping(value = "/close")
    public Result<MrpRectificationTask> close(@RequestParam(name = "id", required = true) String id) {
        service.close(id);
        return Result.ok("已关闭！");
    }

    @Operation(summary = "整改任务通过id删除", description = "整改任务通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpRectificationTask> delete(@RequestParam(name = "id", required = true) String id) {
        MrpRectificationTask byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "整改任务批量删除", description = "整改任务批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpRectificationTask> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "整改任务通过id查询", description = "整改任务通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpRectificationTask> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpRectificationTask mrpRectificationTask = service.getById(id);
        if (mrpRectificationTask == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpRectificationTask);
    }
}

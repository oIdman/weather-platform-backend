package org.jeecg.modules.project.execution.progress.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.execution.progress.entity.MrpProgressReport;
import org.jeecg.modules.project.execution.progress.service.IMrpProgressReportService;
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
import java.util.List;

/**
 * @Description: 项目进展报告（填报 / 甘特图数据）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "项目进展报告")
@RestController
@RequestMapping("/project/execution/progress")
@Slf4j
public class MrpProgressReportController extends JeecgController<MrpProgressReport, IMrpProgressReportService> {

    @Operation(summary = "进展报告分页列表查询", description = "可按项目/报告期过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpProgressReport>> queryPageList(MrpProgressReport mrpProgressReport,
                                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest req) {
        QueryWrapper<MrpProgressReport> queryWrapper = QueryGenerator.initQueryWrapper(mrpProgressReport, req.getParameterMap());
        queryWrapper.orderByAsc("report_period").orderByAsc("create_time");
        Page<MrpProgressReport> page = new Page<>(pageNo, pageSize);
        IPage<MrpProgressReport> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "甘特图数据", description = "按项目返回进展报告序列（报告期/完成度/提交时间），供前端绘制甘特图")
    @GetMapping(value = "/gantt")
    public Result<List<MrpProgressReport>> gantt(@RequestParam(name = "projectId", required = true) String projectId) {
        List<MrpProgressReport> list = service.list(
                new QueryWrapper<MrpProgressReport>().eq("project_id", projectId)
                        .orderByAsc("report_period").orderByAsc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "进展报告添加", description = "默认草稿，完成度 0-100")
    @PostMapping(value = "/add")
    public Result<MrpProgressReport> add(@RequestBody MrpProgressReport mrpProgressReport) {
        service.save(mrpProgressReport);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "进展报告编辑", description = "进展报告编辑")
    @PutMapping(value = "/edit")
    public Result<MrpProgressReport> edit(@RequestBody MrpProgressReport mrpProgressReport) {
        MrpProgressReport byId = service.getById(mrpProgressReport.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpProgressReport);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "进展报告提交", description = "草稿 → 已提交")
    @PostMapping(value = "/submit")
    public Result<MrpProgressReport> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "进展报告通过id删除", description = "进展报告通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpProgressReport> delete(@RequestParam(name = "id", required = true) String id) {
        MrpProgressReport byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "进展报告批量删除", description = "进展报告批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpProgressReport> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "进展报告通过id查询", description = "进展报告通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpProgressReport> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpProgressReport mrpProgressReport = service.getById(id);
        if (mrpProgressReport == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpProgressReport);
    }
}

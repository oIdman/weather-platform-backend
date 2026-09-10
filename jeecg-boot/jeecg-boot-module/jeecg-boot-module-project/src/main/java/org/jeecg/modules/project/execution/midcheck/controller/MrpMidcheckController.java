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
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReport;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReview;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReportService;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReviewService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Description: 中期检查（报告 + 查重）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "中期检查")
@RestController
@RequestMapping("/project/execution/midcheck")
@Slf4j
public class MrpMidcheckController extends JeecgController<MrpMidcheckReport, IMrpMidcheckReportService> {

    @Autowired
    private IMrpMidcheckReviewService midcheckReviewService;

    @Operation(summary = "中期报告分页列表查询", description = "可按项目/状态过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpMidcheckReport>> queryPageList(MrpMidcheckReport mrpMidcheckReport,
                                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                          HttpServletRequest req) {
        QueryWrapper<MrpMidcheckReport> queryWrapper = QueryGenerator.initQueryWrapper(mrpMidcheckReport, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpMidcheckReport> page = new Page<>(pageNo, pageSize);
        IPage<MrpMidcheckReport> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "中期报告添加", description = "默认草稿")
    @PostMapping(value = "/add")
    public Result<MrpMidcheckReport> add(@RequestBody MrpMidcheckReport mrpMidcheckReport) {
        service.save(mrpMidcheckReport);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "中期报告编辑", description = "仅草稿可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpMidcheckReport> edit(@RequestBody MrpMidcheckReport mrpMidcheckReport) {
        MrpMidcheckReport byId = service.getById(mrpMidcheckReport.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可编辑");
        }
        service.updateById(mrpMidcheckReport);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "中期报告提交", description = "草稿 → 已提交（自动查重 vs 任务书）")
    @PostMapping(value = "/submit")
    public Result<MrpMidcheckReport> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "中检评审记录查询", description = "按中期报告ID查询评审记录")
    @GetMapping(value = "/reviewList")
    public Result<List<MrpMidcheckReview>> reviewList(@RequestParam(name = "midcheckReportId", required = true) String midcheckReportId) {
        List<MrpMidcheckReview> list = midcheckReviewService.list(
                new QueryWrapper<MrpMidcheckReview>().eq("midcheck_report_id", midcheckReportId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "中期报告通过id查询", description = "中期报告通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpMidcheckReport> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpMidcheckReport mrpMidcheckReport = service.getById(id);
        if (mrpMidcheckReport == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpMidcheckReport);
    }

    @Operation(summary = "中期报告删除", description = "仅草稿可删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpMidcheckReport> delete(@RequestParam(name = "id", required = true) String id) {
        MrpMidcheckReport byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可删除");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "中期报告批量删除", description = "中期报告批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpMidcheckReport> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        for (String id : Arrays.asList(ids.split(","))) {
            MrpMidcheckReport byId = service.getById(id);
            if (byId != null && !Integer.valueOf(0).equals(byId.getStatus())) {
                return Result.error("仅草稿状态可删除");
            }
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }
}

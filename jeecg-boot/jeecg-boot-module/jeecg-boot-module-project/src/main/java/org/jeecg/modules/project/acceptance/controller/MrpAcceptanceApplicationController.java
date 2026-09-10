package org.jeecg.modules.project.acceptance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceApplication;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceApplicationService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 验收申请管理（提交 / 初审，AI 占位）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Tag(name = "验收申请")
@RestController
@RequestMapping("/project/acceptance/application")
@Slf4j
public class MrpAcceptanceApplicationController extends JeecgController<MrpAcceptanceApplication, IMrpAcceptanceApplicationService> {

    @Operation(summary = "验收申请分页列表查询", description = "可按项目/状态过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpAcceptanceApplication>> queryPageList(MrpAcceptanceApplication mrpAcceptanceApplication,
                                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                 HttpServletRequest req) {
        QueryWrapper<MrpAcceptanceApplication> queryWrapper = QueryGenerator.initQueryWrapper(mrpAcceptanceApplication, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpAcceptanceApplication> page = new Page<>(pageNo, pageSize);
        IPage<MrpAcceptanceApplication> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "验收申请添加", description = "默认草稿")
    @PostMapping(value = "/add")
    public Result<MrpAcceptanceApplication> add(@RequestBody MrpAcceptanceApplication mrpAcceptanceApplication) {
        service.save(mrpAcceptanceApplication);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "验收申请编辑", description = "仅草稿可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpAcceptanceApplication> edit(@RequestBody MrpAcceptanceApplication mrpAcceptanceApplication) {
        MrpAcceptanceApplication byId = service.getById(mrpAcceptanceApplication.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可编辑");
        }
        service.updateById(mrpAcceptanceApplication);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "验收申请提交", description = "草稿 → 已提交")
    @PostMapping(value = "/submit")
    public Result<MrpAcceptanceApplication> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "验收初审", description = "通过 → 待专家评审；不通过 → 退回")
    @PostMapping(value = "/preReview")
    public Result<MrpAcceptanceApplication> preReview(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        Boolean pass = (Boolean) body.get("pass");
        String opinion = (String) body.get("opinion");
        if (id == null || pass == null) {
            return Result.error("参数不完整！");
        }
        service.preReview(id, pass, opinion);
        return Result.ok("初审完成！");
    }

    @Operation(summary = "AI 预验收（占位）", description = "AI 预验收问题清单、通过率预测为二期 AI 能力")
    @PostMapping(value = "/aiPreAccept")
    public Result<Map<String, String>> aiPreAccept(@RequestParam(name = "projectId", required = true) String projectId) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "AI 预验收（问题清单生成）、通过率预测为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }

    @Operation(summary = "通过率预测（占位）", description = "基于历史数据预测验收通过率，二期 AI 能力")
    @PostMapping(value = "/predictPassRate")
    public Result<Map<String, String>> predictPassRate(@RequestParam(name = "projectId", required = true) String projectId) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "通过率预测为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }

    @Operation(summary = "验收申请通过id查询", description = "验收申请通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpAcceptanceApplication> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceApplication mrpAcceptanceApplication = service.getById(id);
        if (mrpAcceptanceApplication == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpAcceptanceApplication);
    }

    @Operation(summary = "验收申请删除", description = "仅草稿可删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpAcceptanceApplication> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceApplication byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可删除");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "验收申请批量删除", description = "验收申请批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpAcceptanceApplication> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        for (String id : Arrays.asList(ids.split(","))) {
            MrpAcceptanceApplication byId = service.getById(id);
            if (byId != null && !Integer.valueOf(0).equals(byId.getStatus())) {
                return Result.error("仅草稿状态可删除");
            }
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }
}

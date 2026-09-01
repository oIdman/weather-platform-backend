package org.jeecg.modules.project.guide.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.guide.entity.MrpGuide;
import org.jeecg.modules.project.guide.entity.MrpGuideVersion;
import org.jeecg.modules.project.guide.enums.GuideStatusEnum;
import org.jeecg.modules.project.guide.service.IMrpGuideService;
import org.jeecg.modules.project.guide.service.IMrpGuideVersionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 课题指南管理（状态机：草稿→待发布→已发布→已归档；发布生成版本快照）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "课题指南管理")
@RestController
@RequestMapping("/project/guide")
@Slf4j
public class MrpGuideController extends JeecgController<MrpGuide, IMrpGuideService> {

    @Autowired
    private IMrpGuideVersionService mrpGuideVersionService;

    @Operation(summary = "指南分页列表查询", description = "指南分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpGuide>> queryPageList(MrpGuide mrpGuide,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<MrpGuide> queryWrapper = QueryGenerator.initQueryWrapper(mrpGuide, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpGuide> page = new Page<>(pageNo, pageSize);
        IPage<MrpGuide> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "指南添加", description = "指南添加（默认草稿状态）")
    @PostMapping(value = "/add")
    public Result<MrpGuide> add(@RequestBody MrpGuide mrpGuide) {
        if (mrpGuide.getStatus() == null) {
            mrpGuide.setStatus(GuideStatusEnum.DRAFT.getCode());
        }
        service.save(mrpGuide);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "指南编辑", description = "仅草稿 / 待发布状态可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpGuide> edit(@RequestBody MrpGuide mrpGuide) {
        MrpGuide byId = service.getById(mrpGuide.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.checkEditable(byId);
        service.updateById(mrpGuide);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "指南通过id删除", description = "仅草稿 / 待发布状态可删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpGuide> delete(@RequestParam(name = "id", required = true) String id) {
        MrpGuide byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.checkEditable(byId);
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "指南批量删除", description = "逐条校验状态后删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpGuide> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            MrpGuide byId = service.getById(id);
            if (byId != null) {
                service.checkEditable(byId);
            }
        }
        service.removeByIds(idList);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "指南通过id查询", description = "指南通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpGuide> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpGuide mrpGuide = service.getById(id);
        if (mrpGuide == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpGuide);
    }

    @Operation(summary = "提交发布", description = "草稿 → 待发布")
    @PostMapping(value = "/submit")
    public Result<MrpGuide> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "发布指南", description = "待发布 → 已发布，自动生成版本快照")
    @PostMapping(value = "/publish")
    public Result<MrpGuide> publish(@RequestParam(name = "id", required = true) String id) {
        service.publish(id);
        return Result.ok("发布成功！");
    }

    @Operation(summary = "指南归档", description = "已发布 → 已归档")
    @PostMapping(value = "/archive")
    public Result<MrpGuide> archive(@RequestParam(name = "id", required = true) String id) {
        service.archive(id);
        return Result.ok("归档成功！");
    }

    @Operation(summary = "版本列表", description = "按指南ID查询版本快照列表")
    @GetMapping(value = "/versionList")
    public Result<List<MrpGuideVersion>> versionList(@RequestParam(name = "guideId", required = true) String guideId) {
        List<MrpGuideVersion> list = mrpGuideVersionService.list(
                new QueryWrapper<MrpGuideVersion>().eq("guide_id", guideId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "版本详情", description = "按版本ID查询版本快照")
    @GetMapping(value = "/versionDetail")
    public Result<MrpGuideVersion> versionDetail(@RequestParam(name = "id", required = true) String id) {
        MrpGuideVersion version = mrpGuideVersionService.getById(id);
        if (version == null) {
            return Result.error("版本不存在");
        }
        return Result.ok(version);
    }

    @Operation(summary = "版本差异对比", description = "返回两个版本的正文，供前端做差异渲染")
    @GetMapping(value = "/versionDiff")
    public Result<Map<String, Object>> versionDiff(@RequestParam(name = "v1", required = true) String v1,
                                                   @RequestParam(name = "v2", required = true) String v2) {
        MrpGuideVersion version1 = mrpGuideVersionService.getById(v1);
        MrpGuideVersion version2 = mrpGuideVersionService.getById(v2);
        if (version1 == null || version2 == null) {
            return Result.error("版本不存在");
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("v1", version1);
        data.put("v2", version2);
        return Result.ok(data);
    }
}

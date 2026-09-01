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
import org.jeecg.modules.project.guide.entity.MrpGuideAcceptanceIndex;
import org.jeecg.modules.project.guide.service.IMrpGuideAcceptanceIndexService;
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
 * @Description: 验收考核指标项管理（按课题分类预置）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "指南验收考核指标")
@RestController
@RequestMapping("/project/guide/acceptanceIndex")
@Slf4j
public class MrpGuideAcceptanceIndexController extends JeecgController<MrpGuideAcceptanceIndex, IMrpGuideAcceptanceIndexService> {

    @Operation(summary = "指标分页列表查询", description = "指标分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpGuideAcceptanceIndex>> queryPageList(MrpGuideAcceptanceIndex mrpGuideAcceptanceIndex,
                                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                HttpServletRequest req) {
        QueryWrapper<MrpGuideAcceptanceIndex> queryWrapper = QueryGenerator.initQueryWrapper(mrpGuideAcceptanceIndex, req.getParameterMap());
        queryWrapper.orderByAsc("sort_order");
        Page<MrpGuideAcceptanceIndex> page = new Page<>(pageNo, pageSize);
        IPage<MrpGuideAcceptanceIndex> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "按指南查询指标", description = "按指南ID查询验收考核指标列表")
    @GetMapping(value = "/queryByGuideId")
    public Result<List<MrpGuideAcceptanceIndex>> queryByGuideId(@RequestParam(name = "guideId", required = true) String guideId) {
        List<MrpGuideAcceptanceIndex> list = service.list(
                new QueryWrapper<MrpGuideAcceptanceIndex>().eq("guide_id", guideId).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "指标添加", description = "指标添加")
    @PostMapping(value = "/add")
    public Result<MrpGuideAcceptanceIndex> add(@RequestBody MrpGuideAcceptanceIndex mrpGuideAcceptanceIndex) {
        service.save(mrpGuideAcceptanceIndex);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "指标编辑", description = "指标编辑")
    @PutMapping(value = "/edit")
    public Result<MrpGuideAcceptanceIndex> edit(@RequestBody MrpGuideAcceptanceIndex mrpGuideAcceptanceIndex) {
        MrpGuideAcceptanceIndex byId = service.getById(mrpGuideAcceptanceIndex.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpGuideAcceptanceIndex);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "指标通过id删除", description = "指标通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpGuideAcceptanceIndex> delete(@RequestParam(name = "id", required = true) String id) {
        MrpGuideAcceptanceIndex byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "指标批量删除", description = "指标批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpGuideAcceptanceIndex> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "指标通过id查询", description = "指标通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpGuideAcceptanceIndex> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpGuideAcceptanceIndex mrpGuideAcceptanceIndex = service.getById(id);
        if (mrpGuideAcceptanceIndex == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpGuideAcceptanceIndex);
    }
}

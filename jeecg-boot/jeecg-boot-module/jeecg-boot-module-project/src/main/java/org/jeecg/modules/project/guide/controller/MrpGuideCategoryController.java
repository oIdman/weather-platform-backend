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
import org.jeecg.modules.project.guide.entity.MrpGuideCategory;
import org.jeecg.modules.project.guide.service.IMrpGuideCategoryService;
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
 * @Description: 指南类目管理
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "指南类目")
@RestController
@RequestMapping("/project/guide/category")
@Slf4j
public class MrpGuideCategoryController extends JeecgController<MrpGuideCategory, IMrpGuideCategoryService> {

    @Operation(summary = "类目分页列表查询", description = "类目分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpGuideCategory>> queryPageList(MrpGuideCategory mrpGuideCategory,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<MrpGuideCategory> queryWrapper = QueryGenerator.initQueryWrapper(mrpGuideCategory, req.getParameterMap());
        queryWrapper.orderByAsc("sort_order");
        Page<MrpGuideCategory> page = new Page<>(pageNo, pageSize);
        IPage<MrpGuideCategory> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "按指南查询类目", description = "按指南ID查询类目列表")
    @GetMapping(value = "/queryByGuideId")
    public Result<List<MrpGuideCategory>> queryByGuideId(@RequestParam(name = "guideId", required = true) String guideId) {
        List<MrpGuideCategory> list = service.list(
                new QueryWrapper<MrpGuideCategory>().eq("guide_id", guideId).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "类目添加", description = "类目添加")
    @PostMapping(value = "/add")
    public Result<MrpGuideCategory> add(@RequestBody MrpGuideCategory mrpGuideCategory) {
        service.save(mrpGuideCategory);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "类目编辑", description = "类目编辑")
    @PutMapping(value = "/edit")
    public Result<MrpGuideCategory> edit(@RequestBody MrpGuideCategory mrpGuideCategory) {
        MrpGuideCategory byId = service.getById(mrpGuideCategory.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpGuideCategory);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "类目通过id删除", description = "类目通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpGuideCategory> delete(@RequestParam(name = "id", required = true) String id) {
        MrpGuideCategory byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "类目批量删除", description = "类目批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpGuideCategory> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "类目通过id查询", description = "类目通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpGuideCategory> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpGuideCategory mrpGuideCategory = service.getById(id);
        if (mrpGuideCategory == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpGuideCategory);
    }
}

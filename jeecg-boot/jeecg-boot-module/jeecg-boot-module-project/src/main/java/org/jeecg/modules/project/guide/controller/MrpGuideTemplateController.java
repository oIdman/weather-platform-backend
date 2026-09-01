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
import org.jeecg.modules.project.guide.entity.MrpGuideTemplate;
import org.jeecg.modules.project.guide.service.IMrpGuideTemplateService;
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

/**
 * @Description: 指南模板库管理（年度 / 阶段模板）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "指南模板库")
@RestController
@RequestMapping("/project/guide/template")
@Slf4j
public class MrpGuideTemplateController extends JeecgController<MrpGuideTemplate, IMrpGuideTemplateService> {

    @Operation(summary = "模板分页列表查询", description = "模板分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpGuideTemplate>> queryPageList(MrpGuideTemplate mrpGuideTemplate,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<MrpGuideTemplate> queryWrapper = QueryGenerator.initQueryWrapper(mrpGuideTemplate, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpGuideTemplate> page = new Page<>(pageNo, pageSize);
        IPage<MrpGuideTemplate> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "模板添加", description = "模板添加")
    @PostMapping(value = "/add")
    public Result<MrpGuideTemplate> add(@RequestBody MrpGuideTemplate mrpGuideTemplate) {
        service.save(mrpGuideTemplate);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "模板编辑", description = "模板编辑")
    @PutMapping(value = "/edit")
    public Result<MrpGuideTemplate> edit(@RequestBody MrpGuideTemplate mrpGuideTemplate) {
        MrpGuideTemplate byId = service.getById(mrpGuideTemplate.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpGuideTemplate);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "模板通过id删除", description = "模板通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpGuideTemplate> delete(@RequestParam(name = "id", required = true) String id) {
        MrpGuideTemplate byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "模板批量删除", description = "模板批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpGuideTemplate> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "模板通过id查询", description = "模板通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpGuideTemplate> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpGuideTemplate mrpGuideTemplate = service.getById(id);
        if (mrpGuideTemplate == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpGuideTemplate);
    }
}

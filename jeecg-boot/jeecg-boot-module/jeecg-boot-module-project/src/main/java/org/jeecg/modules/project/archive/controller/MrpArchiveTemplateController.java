package org.jeecg.modules.project.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.archive.entity.MrpArchiveTemplate;
import org.jeecg.modules.project.archive.service.IMrpArchiveTemplateService;
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
 * @Description: 归档模板管理（按项目类别）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Tag(name = "归档模板")
@RestController
@RequestMapping("/project/archive/template")
@Slf4j
public class MrpArchiveTemplateController extends JeecgController<MrpArchiveTemplate, IMrpArchiveTemplateService> {

    @Operation(summary = "模板分页列表查询", description = "可按类别过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpArchiveTemplate>> queryPageList(MrpArchiveTemplate mrpArchiveTemplate,
                                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                           HttpServletRequest req) {
        QueryWrapper<MrpArchiveTemplate> queryWrapper = QueryGenerator.initQueryWrapper(mrpArchiveTemplate, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpArchiveTemplate> page = new Page<>(pageNo, pageSize);
        IPage<MrpArchiveTemplate> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "模板添加", description = "模板添加")
    @PostMapping(value = "/add")
    public Result<MrpArchiveTemplate> add(@RequestBody MrpArchiveTemplate mrpArchiveTemplate) {
        service.save(mrpArchiveTemplate);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "模板编辑", description = "模板编辑")
    @PutMapping(value = "/edit")
    public Result<MrpArchiveTemplate> edit(@RequestBody MrpArchiveTemplate mrpArchiveTemplate) {
        MrpArchiveTemplate byId = service.getById(mrpArchiveTemplate.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpArchiveTemplate);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "模板通过id删除", description = "模板通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpArchiveTemplate> delete(@RequestParam(name = "id", required = true) String id) {
        MrpArchiveTemplate byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "模板批量删除", description = "模板批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpArchiveTemplate> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "模板通过id查询", description = "模板通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpArchiveTemplate> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpArchiveTemplate mrpArchiveTemplate = service.getById(id);
        if (mrpArchiveTemplate == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpArchiveTemplate);
    }
}

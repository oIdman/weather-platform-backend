package org.jeecg.modules.project.expert.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.expert.entity.MrpExpert;
import org.jeecg.modules.project.expert.service.IMrpExpertService;
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
 * @Description: 专家库（最小化脚手架）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Tag(name = "专家库")
@RestController
@RequestMapping("/project/expert")
@Slf4j
public class MrpExpertController extends JeecgController<MrpExpert, IMrpExpertService> {

    @Operation(summary = "专家分页列表查询", description = "专家分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpExpert>> queryPageList(MrpExpert mrpExpert,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<MrpExpert> queryWrapper = QueryGenerator.initQueryWrapper(mrpExpert, req.getParameterMap());
        Page<MrpExpert> page = new Page<>(pageNo, pageSize);
        IPage<MrpExpert> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "专家添加", description = "专家添加")
    @PostMapping(value = "/add")
    public Result<MrpExpert> add(@RequestBody MrpExpert mrpExpert) {
        service.save(mrpExpert);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "专家编辑", description = "专家编辑")
    @PutMapping(value = "/edit")
    public Result<MrpExpert> edit(@RequestBody MrpExpert mrpExpert) {
        MrpExpert byId = service.getById(mrpExpert.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpExpert);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "专家通过id删除", description = "专家通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpExpert> delete(@RequestParam(name = "id", required = true) String id) {
        MrpExpert byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "专家批量删除", description = "专家批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpExpert> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "专家通过id查询", description = "专家通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpExpert> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpExpert mrpExpert = service.getById(id);
        if (mrpExpert == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpExpert);
    }
}

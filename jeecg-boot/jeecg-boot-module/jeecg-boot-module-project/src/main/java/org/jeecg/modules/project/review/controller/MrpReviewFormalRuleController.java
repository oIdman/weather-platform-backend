package org.jeecg.modules.project.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRule;
import org.jeecg.modules.project.review.service.IMrpReviewFormalRuleService;
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
 * @Description: 形式审查规则库管理（可配置）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "形式审查规则库")
@RestController
@RequestMapping("/project/review/formalRule")
@Slf4j
public class MrpReviewFormalRuleController extends JeecgController<MrpReviewFormalRule, IMrpReviewFormalRuleService> {

    @Operation(summary = "规则分页列表查询", description = "规则分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpReviewFormalRule>> queryPageList(MrpReviewFormalRule mrpReviewFormalRule,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<MrpReviewFormalRule> queryWrapper = QueryGenerator.initQueryWrapper(mrpReviewFormalRule, req.getParameterMap());
        queryWrapper.orderByAsc("sort_order");
        Page<MrpReviewFormalRule> page = new Page<>(pageNo, pageSize);
        IPage<MrpReviewFormalRule> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "规则添加", description = "规则添加")
    @PostMapping(value = "/add")
    public Result<MrpReviewFormalRule> add(@RequestBody MrpReviewFormalRule mrpReviewFormalRule) {
        service.save(mrpReviewFormalRule);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "规则编辑", description = "规则编辑")
    @PutMapping(value = "/edit")
    public Result<MrpReviewFormalRule> edit(@RequestBody MrpReviewFormalRule mrpReviewFormalRule) {
        MrpReviewFormalRule byId = service.getById(mrpReviewFormalRule.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpReviewFormalRule);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "规则通过id删除", description = "规则通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpReviewFormalRule> delete(@RequestParam(name = "id", required = true) String id) {
        MrpReviewFormalRule byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "规则批量删除", description = "规则批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpReviewFormalRule> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "规则通过id查询", description = "规则通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpReviewFormalRule> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpReviewFormalRule mrpReviewFormalRule = service.getById(id);
        if (mrpReviewFormalRule == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpReviewFormalRule);
    }
}

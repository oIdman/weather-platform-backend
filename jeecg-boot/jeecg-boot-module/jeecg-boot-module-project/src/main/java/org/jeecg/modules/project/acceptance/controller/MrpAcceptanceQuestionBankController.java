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
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceQuestionBank;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceQuestionBankService;
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
 * @Description: 验收问题库（专家评审辅助）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Tag(name = "验收问题库")
@RestController
@RequestMapping("/project/acceptance/questionBank")
@Slf4j
public class MrpAcceptanceQuestionBankController extends JeecgController<MrpAcceptanceQuestionBank, IMrpAcceptanceQuestionBankService> {

    @Operation(summary = "问题分页列表查询", description = "可按分类过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpAcceptanceQuestionBank>> queryPageList(MrpAcceptanceQuestionBank mrpAcceptanceQuestionBank,
                                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                  HttpServletRequest req) {
        QueryWrapper<MrpAcceptanceQuestionBank> queryWrapper = QueryGenerator.initQueryWrapper(mrpAcceptanceQuestionBank, req.getParameterMap());
        queryWrapper.orderByAsc("sort_order");
        Page<MrpAcceptanceQuestionBank> page = new Page<>(pageNo, pageSize);
        IPage<MrpAcceptanceQuestionBank> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "按分类查询问题", description = "按分类查询验收问题")
    @GetMapping(value = "/queryByCategory")
    public Result<List<MrpAcceptanceQuestionBank>> queryByCategory(@RequestParam(name = "category", required = true) String category) {
        List<MrpAcceptanceQuestionBank> list = service.list(
                new QueryWrapper<MrpAcceptanceQuestionBank>().eq("category", category).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "问题添加", description = "问题添加")
    @PostMapping(value = "/add")
    public Result<MrpAcceptanceQuestionBank> add(@RequestBody MrpAcceptanceQuestionBank mrpAcceptanceQuestionBank) {
        service.save(mrpAcceptanceQuestionBank);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "问题编辑", description = "问题编辑")
    @PutMapping(value = "/edit")
    public Result<MrpAcceptanceQuestionBank> edit(@RequestBody MrpAcceptanceQuestionBank mrpAcceptanceQuestionBank) {
        MrpAcceptanceQuestionBank byId = service.getById(mrpAcceptanceQuestionBank.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpAcceptanceQuestionBank);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "问题通过id删除", description = "问题通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpAcceptanceQuestionBank> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceQuestionBank byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "问题批量删除", description = "问题批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpAcceptanceQuestionBank> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "问题通过id查询", description = "问题通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpAcceptanceQuestionBank> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceQuestionBank mrpAcceptanceQuestionBank = service.getById(id);
        if (mrpAcceptanceQuestionBank == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpAcceptanceQuestionBank);
    }
}

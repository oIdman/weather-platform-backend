package org.jeecg.modules.project.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.application.entity.MrpApplicationMember;
import org.jeecg.modules.project.application.service.IMrpApplicationMemberService;
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
 * @Description: 申报团队成员管理
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "申报团队成员")
@RestController
@RequestMapping("/project/application/member")
@Slf4j
public class MrpApplicationMemberController extends JeecgController<MrpApplicationMember, IMrpApplicationMemberService> {

    @Operation(summary = "成员分页列表查询", description = "成员分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpApplicationMember>> queryPageList(MrpApplicationMember mrpApplicationMember,
                                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             HttpServletRequest req) {
        QueryWrapper<MrpApplicationMember> queryWrapper = QueryGenerator.initQueryWrapper(mrpApplicationMember, req.getParameterMap());
        queryWrapper.orderByAsc("sort_order");
        Page<MrpApplicationMember> page = new Page<>(pageNo, pageSize);
        IPage<MrpApplicationMember> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "按申报查询成员", description = "按申报ID查询团队成员列表")
    @GetMapping(value = "/queryByApplicationId")
    public Result<List<MrpApplicationMember>> queryByApplicationId(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpApplicationMember> list = service.list(
                new QueryWrapper<MrpApplicationMember>().eq("application_id", applicationId).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "成员添加", description = "成员添加")
    @PostMapping(value = "/add")
    public Result<MrpApplicationMember> add(@RequestBody MrpApplicationMember mrpApplicationMember) {
        service.save(mrpApplicationMember);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "成员编辑", description = "成员编辑")
    @PutMapping(value = "/edit")
    public Result<MrpApplicationMember> edit(@RequestBody MrpApplicationMember mrpApplicationMember) {
        MrpApplicationMember byId = service.getById(mrpApplicationMember.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpApplicationMember);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "成员通过id删除", description = "成员通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpApplicationMember> delete(@RequestParam(name = "id", required = true) String id) {
        MrpApplicationMember byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "成员批量删除", description = "成员批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpApplicationMember> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "成员通过id查询", description = "成员通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpApplicationMember> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpApplicationMember mrpApplicationMember = service.getById(id);
        if (mrpApplicationMember == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpApplicationMember);
    }
}

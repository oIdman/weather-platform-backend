package org.jeecg.modules.project.execution.change.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.execution.change.entity.MrpChangeRequest;
import org.jeecg.modules.project.execution.change.service.IMrpChangeRequestService;
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
 * @Description: 项目变更管理（影响评估规则）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "项目变更管理")
@RestController
@RequestMapping("/project/execution/change")
@Slf4j
public class MrpChangeRequestController extends JeecgController<MrpChangeRequest, IMrpChangeRequestService> {

    @Operation(summary = "变更分页列表查询", description = "可按项目/状态/类型过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpChangeRequest>> queryPageList(MrpChangeRequest mrpChangeRequest,
                                                         @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest req) {
        QueryWrapper<MrpChangeRequest> queryWrapper = QueryGenerator.initQueryWrapper(mrpChangeRequest, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpChangeRequest> page = new Page<>(pageNo, pageSize);
        IPage<MrpChangeRequest> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "变更申请添加", description = "默认草稿")
    @PostMapping(value = "/add")
    public Result<MrpChangeRequest> add(@RequestBody MrpChangeRequest mrpChangeRequest) {
        service.save(mrpChangeRequest);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "变更申请编辑", description = "仅草稿可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpChangeRequest> edit(@RequestBody MrpChangeRequest mrpChangeRequest) {
        MrpChangeRequest byId = service.getById(mrpChangeRequest.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可编辑");
        }
        service.updateById(mrpChangeRequest);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "提交变更", description = "规则评估影响等级（预算/关键人员/方向变更→高风险）")
    @PostMapping(value = "/submit")
    public Result<MrpChangeRequest> submit(@RequestParam(name = "id", required = true) String id) {
        MrpChangeRequest change = service.submit(id);
        return Result.OK("提交成功！", change);
    }

    @Operation(summary = "变更申请通过id查询", description = "变更申请通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpChangeRequest> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpChangeRequest mrpChangeRequest = service.getById(id);
        if (mrpChangeRequest == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpChangeRequest);
    }

    @Operation(summary = "变更申请删除", description = "仅草稿可删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpChangeRequest> delete(@RequestParam(name = "id", required = true) String id) {
        MrpChangeRequest byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可删除");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "变更申请批量删除", description = "变更申请批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpChangeRequest> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        for (String id : Arrays.asList(ids.split(","))) {
            MrpChangeRequest byId = service.getById(id);
            if (byId != null && !Integer.valueOf(0).equals(byId.getStatus())) {
                return Result.error("仅草稿状态可删除");
            }
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }
}

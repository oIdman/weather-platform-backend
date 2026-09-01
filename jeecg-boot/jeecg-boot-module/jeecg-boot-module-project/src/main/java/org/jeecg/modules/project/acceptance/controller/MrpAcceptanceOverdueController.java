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
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceOverdue;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceOverdueService;
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
import java.util.Map;

/**
 * @Description: 验收延期处理（惩罚机制，限制申报联动 Phase 2 冲突预检）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Tag(name = "验收延期处理")
@RestController
@RequestMapping("/project/acceptance/overdue")
@Slf4j
public class MrpAcceptanceOverdueController extends JeecgController<MrpAcceptanceOverdue, IMrpAcceptanceOverdueService> {

    @Operation(summary = "延期记录分页列表查询", description = "可按项目/状态过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpAcceptanceOverdue>> queryPageList(MrpAcceptanceOverdue mrpAcceptanceOverdue,
                                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             HttpServletRequest req) {
        QueryWrapper<MrpAcceptanceOverdue> queryWrapper = QueryGenerator.initQueryWrapper(mrpAcceptanceOverdue, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpAcceptanceOverdue> page = new Page<>(pageNo, pageSize);
        IPage<MrpAcceptanceOverdue> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "延期记录添加", description = "记录项目延期情况，默认处理中")
    @PostMapping(value = "/add")
    public Result<MrpAcceptanceOverdue> add(@RequestBody MrpAcceptanceOverdue mrpAcceptanceOverdue) {
        service.save(mrpAcceptanceOverdue);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "延期记录编辑", description = "延期记录编辑")
    @PutMapping(value = "/edit")
    public Result<MrpAcceptanceOverdue> edit(@RequestBody MrpAcceptanceOverdue mrpAcceptanceOverdue) {
        MrpAcceptanceOverdue byId = service.getById(mrpAcceptanceOverdue.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpAcceptanceOverdue);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "延期处理", description = "已通报(1) / 已限制申报(2) / 已整改销号(3)；限制申报状态将进入申报受限名单")
    @PostMapping(value = "/handle")
    public Result<MrpAcceptanceOverdue> handle(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        Integer targetStatus = body.get("status") == null ? null : Integer.parseInt(String.valueOf(body.get("status")));
        String handleResult = (String) body.get("handleResult");
        if (id == null) {
            return Result.error("参数不完整！");
        }
        service.handle(id, targetStatus, handleResult);
        return Result.ok("处理完成！");
    }

    @Operation(summary = "延期记录通过id查询", description = "延期记录通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpAcceptanceOverdue> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceOverdue mrpAcceptanceOverdue = service.getById(id);
        if (mrpAcceptanceOverdue == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpAcceptanceOverdue);
    }

    @Operation(summary = "延期记录删除", description = "延期记录删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpAcceptanceOverdue> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceOverdue byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "延期记录批量删除", description = "延期记录批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpAcceptanceOverdue> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }
}

package org.jeecg.modules.project.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.service.IMrpProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Description: 立项项目主表管理
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "立项项目")
@RestController
@RequestMapping("/project/project")
@Slf4j
public class MrpProjectController extends JeecgController<MrpProject, IMrpProjectService> {

    @Operation(summary = "项目分页列表查询", description = "项目分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpProject>> queryPageList(MrpProject mrpProject,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<MrpProject> queryWrapper = QueryGenerator.initQueryWrapper(mrpProject, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpProject> page = new Page<>(pageNo, pageSize);
        IPage<MrpProject> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "项目通过id查询", description = "项目通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpProject> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpProject mrpProject = service.getById(id);
        if (mrpProject == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpProject);
    }

    @Operation(summary = "立项", description = "评审通过的申报 → 立项项目主表（校验公示结论，触发通知 Mock）")
    @PostMapping(value = "/approve")
    public Result<MrpProject> approve(@RequestBody Map<String, String> body) {
        String applicationId = body.get("applicationId");
        if (applicationId == null) {
            return Result.error("参数不完整！");
        }
        MrpProject project = service.approve(applicationId);
        return Result.OK("立项成功！", project);
    }

    @Operation(summary = "项目信息编辑", description = "调整项目基本信息（起止日期、经费、备注等）")
    @PutMapping(value = "/edit")
    public Result<MrpProject> edit(@RequestBody MrpProject mrpProject) {
        MrpProject byId = service.getById(mrpProject.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpProject);
        return Result.ok("修改成功！");
    }
}

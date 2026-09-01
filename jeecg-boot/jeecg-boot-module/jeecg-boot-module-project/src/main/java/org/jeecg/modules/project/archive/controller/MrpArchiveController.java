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
import org.jeecg.modules.project.archive.entity.MrpArchive;
import org.jeecg.modules.project.archive.entity.MrpArchiveItem;
import org.jeecg.modules.project.archive.entity.MrpArchiveLog;
import org.jeecg.modules.project.archive.service.IMrpArchiveItemService;
import org.jeecg.modules.project.archive.service.IMrpArchiveLogService;
import org.jeecg.modules.project.archive.service.IMrpArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 项目归档管理（准备/完成/材料清单/审计日志，智能检索 AI 占位）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Tag(name = "项目归档")
@RestController
@RequestMapping("/project/archive")
@Slf4j
public class MrpArchiveController extends JeecgController<MrpArchive, IMrpArchiveService> {

    @Autowired
    private IMrpArchiveItemService archiveItemService;

    @Autowired
    private IMrpArchiveLogService archiveLogService;

    @Operation(summary = "归档分页列表查询", description = "可按项目/状态过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpArchive>> queryPageList(MrpArchive mrpArchive,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<MrpArchive> queryWrapper = QueryGenerator.initQueryWrapper(mrpArchive, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpArchive> page = new Page<>(pageNo, pageSize);
        IPage<MrpArchive> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "归档准备", description = "仅已验收项目可归档，自动生成全流程材料清单")
    @PostMapping(value = "/prepare")
    public Result<MrpArchive> prepare(@RequestBody Map<String, String> body) {
        String projectId = body.get("projectId");
        if (projectId == null) {
            return Result.error("参数不完整！");
        }
        MrpArchive archive = service.prepare(projectId);
        return Result.OK("归档准备完成！", archive);
    }

    @Operation(summary = "完成归档", description = "推送电子档案包（Mock 数字档案系统）并联动项目状态为已归档")
    @PostMapping(value = "/complete")
    public Result<MrpArchive> complete(@RequestBody Map<String, String> body) {
        String archiveId = body.get("archiveId");
        if (archiveId == null) {
            return Result.error("参数不完整！");
        }
        service.complete(archiveId);
        return Result.ok("归档完成！");
    }

    @Operation(summary = "归档通过id查询", description = "归档通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpArchive> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpArchive mrpArchive = service.getById(id);
        if (mrpArchive == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpArchive);
    }

    @Operation(summary = "归档材料清单查询", description = "按归档ID查询材料明细")
    @GetMapping(value = "/queryItems")
    public Result<List<MrpArchiveItem>> queryItems(@RequestParam(name = "archiveId", required = true) String archiveId) {
        List<MrpArchiveItem> list = archiveItemService.list(
                new QueryWrapper<MrpArchiveItem>().eq("archive_id", archiveId).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "归档审计日志查询", description = "按归档ID查询查阅/下载/打印日志")
    @GetMapping(value = "/queryLogs")
    public Result<List<MrpArchiveLog>> queryLogs(@RequestParam(name = "archiveId", required = true) String archiveId) {
        List<MrpArchiveLog> list = archiveLogService.list(
                new QueryWrapper<MrpArchiveLog>().eq("archive_id", archiveId).orderByDesc("operate_time"));
        return Result.ok(list);
    }

    @Operation(summary = "智能检索（AI 占位）", description = "自然语言档案检索为二期向量库能力，当前为占位接口")
    @PostMapping(value = "/aiSearch")
    public Result<Map<String, String>> aiSearch(@RequestParam(name = "keyword", required = true) String keyword) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "档案智能检索（自然语言/向量库）为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }
}

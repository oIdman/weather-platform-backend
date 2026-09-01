package org.jeecg.modules.project.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.archive.entity.MrpArchive;
import org.jeecg.modules.project.archive.entity.MrpArchiveLog;
import org.jeecg.modules.project.archive.service.IMrpArchiveLogService;
import org.jeecg.modules.project.archive.service.IMrpArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 归档操作日志（查阅 / 下载 / 打印，安全审计）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Tag(name = "归档操作日志")
@RestController
@RequestMapping("/project/archive/log")
@Slf4j
public class MrpArchiveLogController {

    @Autowired
    private IMrpArchiveLogService archiveLogService;

    @Autowired
    private IMrpArchiveService archiveService;

    @Operation(summary = "记录归档操作", description = "查阅(view)/下载(download)/打印(print)，操作人/时间自动记录")
    @PostMapping(value = "/add")
    public Result<MrpArchiveLog> add(@RequestBody Map<String, String> body) {
        String archiveId = body.get("archiveId");
        String actionType = body.get("actionType");
        if (archiveId == null || actionType == null) {
            return Result.error("参数不完整！");
        }
        MrpArchive archive = archiveService.getById(archiveId);
        if (archive == null) {
            return Result.error("归档记录不存在");
        }
        MrpArchiveLog logRecord = new MrpArchiveLog();
        logRecord.setArchiveId(archiveId);
        logRecord.setProjectId(archive.getProjectId());
        logRecord.setActionType(actionType);
        logRecord.setOperator("admin");
        logRecord.setOperateTime(new Date());
        archiveLogService.save(logRecord);
        return Result.ok("日志记录成功！");
    }

    @Operation(summary = "按归档查询日志", description = "按归档ID查询操作日志")
    @GetMapping(value = "/queryByArchiveId")
    public Result<List<MrpArchiveLog>> queryByArchiveId(@RequestParam(name = "archiveId", required = true) String archiveId) {
        List<MrpArchiveLog> list = archiveLogService.list(
                new QueryWrapper<MrpArchiveLog>().eq("archive_id", archiveId).orderByDesc("operate_time"));
        return Result.ok(list);
    }
}

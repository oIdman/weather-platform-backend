package org.jeecg.modules.project.acceptance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceMeeting;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 验收会议管理（会议室 / 设备预约）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Tag(name = "验收会议")
@RestController
@RequestMapping("/project/acceptance/meeting")
@Slf4j
public class MrpAcceptanceMeetingController {

    @Autowired
    private IMrpAcceptanceMeetingService meetingService;

    @Operation(summary = "会议列表查询", description = "可按验收申请/项目过滤")
    @GetMapping(value = "/list")
    public Result<List<MrpAcceptanceMeeting>> list(@RequestParam(name = "acceptanceApplicationId", required = false) String acceptanceApplicationId,
                                                   @RequestParam(name = "projectId", required = false) String projectId) {
        QueryWrapper<MrpAcceptanceMeeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(acceptanceApplicationId != null, "acceptance_application_id", acceptanceApplicationId)
                .eq(projectId != null, "project_id", projectId)
                .orderByAsc("meeting_time");
        return Result.ok(meetingService.list(queryWrapper));
    }

    @Operation(summary = "按验收申请查询会议", description = "按验收申请ID查询会议")
    @GetMapping(value = "/queryByApplicationId")
    public Result<List<MrpAcceptanceMeeting>> queryByApplicationId(@RequestParam(name = "acceptanceApplicationId", required = true) String acceptanceApplicationId) {
        List<MrpAcceptanceMeeting> list = meetingService.list(
                new QueryWrapper<MrpAcceptanceMeeting>().eq("acceptance_application_id", acceptanceApplicationId).orderByAsc("meeting_time"));
        return Result.ok(list);
    }

    @Operation(summary = "会议添加", description = "默认待组织")
    @PostMapping(value = "/add")
    public Result<MrpAcceptanceMeeting> add(@RequestBody MrpAcceptanceMeeting meeting) {
        meetingService.save(meeting);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "会议编辑", description = "会议编辑")
    @PutMapping(value = "/edit")
    public Result<MrpAcceptanceMeeting> edit(@RequestBody MrpAcceptanceMeeting meeting) {
        MrpAcceptanceMeeting byId = meetingService.getById(meeting.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        meetingService.updateById(meeting);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "会议预约", description = "待组织 → 已预约")
    @PostMapping(value = "/confirm")
    public Result<MrpAcceptanceMeeting> confirm(@RequestParam(name = "id", required = true) String id) {
        meetingService.confirm(id);
        return Result.ok("预约成功！");
    }

    @Operation(summary = "会议召开", description = "待组织/已预约 → 已召开")
    @PostMapping(value = "/hold")
    public Result<MrpAcceptanceMeeting> hold(@RequestParam(name = "id", required = true) String id) {
        meetingService.hold(id);
        return Result.ok("会议已召开！");
    }

    @Operation(summary = "会议通过id查询", description = "会议通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpAcceptanceMeeting> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceMeeting meeting = meetingService.getById(id);
        if (meeting == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(meeting);
    }

    @Operation(summary = "会议删除", description = "会议删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpAcceptanceMeeting> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceMeeting byId = meetingService.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        meetingService.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "会议批量删除", description = "会议批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpAcceptanceMeeting> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        meetingService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }
}

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
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.entity.MrpApplicationConflictCheck;
import org.jeecg.modules.project.application.entity.MrpApplicationVersion;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationConflictCheckService;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.application.service.IMrpApplicationVersionService;
import org.jeecg.modules.project.common.attachment.entity.MrpAttachment;
import org.jeecg.modules.project.common.attachment.service.IMrpAttachmentService;
import org.jeecg.modules.project.common.enums.AttachmentBizTypeEnum;
import org.jeecg.modules.project.researcher.entity.MrpResearcher;
import org.jeecg.modules.project.researcher.entity.MrpResearcherAchievement;
import org.jeecg.modules.project.researcher.service.IMrpResearcherAchievementService;
import org.jeecg.modules.project.researcher.service.IMrpResearcherService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 课题申报管理（履历自动带入 / 冲突预检 / 提交版本快照 / AI 占位）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "课题申报管理")
@RestController
@RequestMapping("/project/application")
@Slf4j
public class MrpApplicationController extends JeecgController<MrpApplication, IMrpApplicationService> {

    @Autowired
    private IMrpApplicationVersionService mrpApplicationVersionService;

    @Autowired
    private IMrpApplicationConflictCheckService mrpApplicationConflictCheckService;

    @Autowired
    private IMrpResearcherService mrpResearcherService;

    @Autowired
    private IMrpResearcherAchievementService mrpResearcherAchievementService;

    @Autowired
    private IMrpAttachmentService mrpAttachmentService;

    @Operation(summary = "申报分页列表查询", description = "申报分页列表查询（可按指南/申请人/状态过滤）")
    @GetMapping(value = "/list")
    public Result<IPage<MrpApplication>> queryPageList(MrpApplication mrpApplication,
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<MrpApplication> queryWrapper = QueryGenerator.initQueryWrapper(mrpApplication, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpApplication> page = new Page<>(pageNo, pageSize);
        IPage<MrpApplication> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "申报添加", description = "自动预填指南信息与申请人履历，默认草稿状态")
    @PostMapping(value = "/add")
    public Result<MrpApplication> add(@RequestBody MrpApplication mrpApplication) {
        if (mrpApplication.getStatus() == null) {
            mrpApplication.setStatus(ApplicationStatusEnum.DRAFT.getCode());
        }
        service.prefillFromGuide(mrpApplication);
        service.prefillApplicant(mrpApplication);
        service.save(mrpApplication);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "申报编辑", description = "仅草稿状态可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpApplication> edit(@RequestBody MrpApplication mrpApplication) {
        MrpApplication byId = service.getById(mrpApplication.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.checkEditable(byId);
        service.updateById(mrpApplication);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "申报通过id删除", description = "仅草稿状态可删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpApplication> delete(@RequestParam(name = "id", required = true) String id) {
        MrpApplication byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.checkEditable(byId);
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "申报批量删除", description = "逐条校验状态后删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpApplication> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            MrpApplication byId = service.getById(id);
            if (byId != null) {
                service.checkEditable(byId);
            }
        }
        service.removeByIds(idList);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "申报通过id查询", description = "申报通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpApplication> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpApplication mrpApplication = service.getById(id);
        if (mrpApplication == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpApplication);
    }

    @Operation(summary = "提交申报", description = "草稿 → 已提交（自动冲突预检 + 版本快照，预检不通过则拦截）")
    @PostMapping(value = "/submit")
    public Result<MrpApplication> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "撤回申报", description = "已提交 → 草稿")
    @PostMapping(value = "/withdraw")
    public Result<MrpApplication> withdraw(@RequestParam(name = "id", required = true) String id) {
        service.withdraw(id);
        return Result.ok("撤回成功！");
    }

    @Operation(summary = "冲突预检", description = "手动触发冲突预检并保存记录")
    @PostMapping(value = "/conflictCheck")
    public Result<List<MrpApplicationConflictCheck>> conflictCheck(@RequestParam(name = "id", required = true) String id) {
        MrpApplication application = service.getById(id);
        if (application == null) {
            return Result.error("未找到对应实体");
        }
        List<MrpApplicationConflictCheck> checks = service.runConflictCheck(application);
        mrpApplicationConflictCheckService.saveBatch(checks);
        return Result.ok(checks);
    }

    @Operation(summary = "冲突预检结果查询", description = "按申报ID查询预检记录")
    @GetMapping(value = "/conflictCheckResult")
    public Result<List<MrpApplicationConflictCheck>> conflictCheckResult(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpApplicationConflictCheck> list = mrpApplicationConflictCheckService.list(
                new QueryWrapper<MrpApplicationConflictCheck>().eq("application_id", applicationId).orderByDesc("check_time"));
        return Result.ok(list);
    }

    @Operation(summary = "申报版本列表", description = "按申报ID查询版本快照列表")
    @GetMapping(value = "/versionList")
    public Result<List<MrpApplicationVersion>> versionList(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpApplicationVersion> list = mrpApplicationVersionService.list(
                new QueryWrapper<MrpApplicationVersion>().eq("application_id", applicationId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "申报版本详情", description = "按版本ID查询版本快照")
    @GetMapping(value = "/versionDetail")
    public Result<MrpApplicationVersion> versionDetail(@RequestParam(name = "id", required = true) String id) {
        MrpApplicationVersion version = mrpApplicationVersionService.getById(id);
        if (version == null) {
            return Result.error("版本不存在");
        }
        return Result.ok(version);
    }

    @Operation(summary = "科研履历查询", description = "按科研人员ID返回档案与既往成果（申报自动带入）")
    @GetMapping(value = "/queryResume")
    public Result<Map<String, Object>> queryResume(@RequestParam(name = "researcherId", required = true) String researcherId) {
        MrpResearcher researcher = mrpResearcherService.getById(researcherId);
        if (researcher == null) {
            return Result.error("科研人员不存在");
        }
        List<MrpResearcherAchievement> achievements = mrpResearcherAchievementService.list(
                new QueryWrapper<MrpResearcherAchievement>().eq("researcher_id", researcherId));
        Map<String, Object> data = new HashMap<>(4);
        data.put("researcher", researcher);
        data.put("achievements", achievements);
        return Result.ok(data);
    }

    @Operation(summary = "申报附件查询", description = "查询该申报的佐证材料（公共附件表 bizType=application）")
    @GetMapping(value = "/queryAttachments")
    public Result<List<MrpAttachment>> queryAttachments(@RequestParam(name = "id", required = true) String id) {
        List<MrpAttachment> list = mrpAttachmentService.list(
                new QueryWrapper<MrpAttachment>()
                        .eq("biz_type", AttachmentBizTypeEnum.APPLICATION.getCode())
                        .eq("biz_id", id));
        return Result.ok(list);
    }

    @Operation(summary = "智能申报助手（AI 占位）", description = "自动填充 / 案例推荐为二期 AI 能力，当前为占位接口")
    @PostMapping(value = "/aiSuggest")
    public Result<Map<String, String>> aiSuggest(@RequestParam(name = "id", required = true) String id) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "智能申报助手（自动填充、案例推荐）为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }
}

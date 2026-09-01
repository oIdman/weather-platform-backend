package org.jeecg.modules.project.application.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceOverdue;
import org.jeecg.modules.project.acceptance.mapper.MrpAcceptanceOverdueMapper;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.entity.MrpApplicationConflictCheck;
import org.jeecg.modules.project.application.entity.MrpApplicationVersion;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.mapper.MrpApplicationMapper;
import org.jeecg.modules.project.application.service.IMrpApplicationConflictCheckService;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.application.service.IMrpApplicationVersionService;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.mapper.MrpProjectMapper;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.guide.entity.MrpGuide;
import org.jeecg.modules.project.guide.service.IMrpGuideService;
import org.jeecg.modules.project.researcher.entity.MrpResearcher;
import org.jeecg.modules.project.researcher.service.IMrpResearcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 课题申报 Service 实现（冲突预检规则引擎 + 提交版本快照）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Service
public class MrpApplicationServiceImpl extends ServiceImpl<MrpApplicationMapper, MrpApplication> implements IMrpApplicationService {

    /** 在研项目数上限 */
    private static final int ONGOING_PROJECT_LIMIT = 3;

    @Autowired
    private IMrpApplicationVersionService applicationVersionService;

    @Autowired
    private IMrpApplicationConflictCheckService conflictCheckService;

    @Autowired
    private IMrpGuideService guideService;

    @Autowired
    private IMrpResearcherService researcherService;

    @Autowired
    private MrpAcceptanceOverdueMapper acceptanceOverdueMapper;

    @Autowired
    private MrpProjectMapper projectMapper;

    @Override
    public void prefillFromGuide(MrpApplication application) {
        if (oConvertUtils.isEmpty(application.getGuideId())) {
            return;
        }
        MrpGuide guide = guideService.getById(application.getGuideId());
        if (guide == null) {
            return;
        }
        application.setGuideTitle(guide.getGuideTitle());
        if (oConvertUtils.isEmpty(application.getProjectType())) {
            application.setProjectType(guide.getProjectType());
        }
    }

    @Override
    public void prefillApplicant(MrpApplication application) {
        if (oConvertUtils.isEmpty(application.getApplicantId())) {
            return;
        }
        MrpResearcher researcher = researcherService.getById(application.getApplicantId());
        if (researcher == null) {
            return;
        }
        application.setApplicantName(researcher.getName());
        application.setApplicantOrg(researcher.getOrgName());
        application.setApplicantTitle(researcher.getTitle());
    }

    @Override
    public void submit(String id) {
        MrpApplication application = getApplicationOrThrow(id);
        checkStatus(application, ApplicationStatusEnum.DRAFT, "仅草稿状态可提交");
        List<MrpApplicationConflictCheck> checks = runConflictCheck(application);
        conflictCheckService.saveBatch(checks);
        boolean allPass = checks.stream().allMatch(c -> Integer.valueOf(1).equals(c.getCheckResult()));
        if (!allPass) {
            String reasons = checks.stream()
                    .filter(c -> !Integer.valueOf(1).equals(c.getCheckResult()))
                    .map(MrpApplicationConflictCheck::getCheckDetail)
                    .collect(Collectors.joining("；"));
            throw new ProjectBizException("冲突预检未通过：" + reasons);
        }
        long count = applicationVersionService.count(new QueryWrapper<MrpApplicationVersion>().eq("application_id", id));
        MrpApplicationVersion version = new MrpApplicationVersion();
        version.setApplicationId(id);
        version.setVersionNo("V" + (count + 1));
        version.setProjectTitle(application.getProjectTitle());
        version.setProjectSummary(application.getProjectSummary());
        version.setContent(buildSnapshotJson(application));
        version.setChangeNote("提交快照");
        applicationVersionService.save(version);
        application.setStatus(ApplicationStatusEnum.SUBMITTED.getCode());
        application.setSubmitTime(new Date());
        updateById(application);
    }

    @Override
    public void withdraw(String id) {
        MrpApplication application = getApplicationOrThrow(id);
        checkStatus(application, ApplicationStatusEnum.SUBMITTED, "仅已提交状态可撤回");
        update(new LambdaUpdateWrapper<MrpApplication>()
                .eq(MrpApplication::getId, id)
                .set(MrpApplication::getStatus, ApplicationStatusEnum.DRAFT.getCode())
                .set(MrpApplication::getSubmitTime, null));
    }

    @Override
    public void checkEditable(MrpApplication application) {
        if (application != null && application.getStatus() != null
                && !ApplicationStatusEnum.DRAFT.getCode().equals(application.getStatus())) {
            throw new ProjectBizException("非草稿状态不可编辑或删除");
        }
    }

    @Override
    public List<MrpApplicationConflictCheck> runConflictCheck(MrpApplication application) {
        List<MrpApplicationConflictCheck> results = new ArrayList<>();
        MrpResearcher researcher = null;
        if (oConvertUtils.isNotEmpty(application.getApplicantId())) {
            researcher = researcherService.getById(application.getApplicantId());
        }
        Date now = new Date();
        int ongoing = researcher != null && researcher.getOngoingProjectCount() != null
                ? researcher.getOngoingProjectCount() : 0;
        boolean ongoingOk = ongoing < ONGOING_PROJECT_LIMIT;
        results.add(buildCheck(application, "ongoing_project_count", ongoingOk,
                "在研项目数(" + ongoing + ")未超过上限(" + ONGOING_PROJECT_LIMIT + ")",
                "在研项目数(" + ongoing + ")超过上限(" + ONGOING_PROJECT_LIMIT + ")", now));

        boolean titleOk = researcher != null && oConvertUtils.isNotEmpty(researcher.getTitle());
        results.add(buildCheck(application, "title_qualification", titleOk,
                "申请人职称信息完整",
                "申请人职称信息缺失，请先在科研人员库维护职称", now));

        boolean overdueOk = !isOverdueRestricted(application.getApplicantId());
        results.add(buildCheck(application, "overdue_restricted", overdueOk,
                "无未验收/延期受限记录",
                "存在未验收/延期受限项目，禁止申报", now));
        return results;
    }

    /**
     * Phase 6 跨期校验点：查询申请人为负责人且存在未销号延期记录（status 0/1/2）的项目。
     */
    private boolean isOverdueRestricted(String applicantId) {
        if (oConvertUtils.isEmpty(applicantId)) {
            return false;
        }
        List<MrpProject> projects = projectMapper.selectList(
                new QueryWrapper<MrpProject>().eq("leader_id", applicantId));
        if (projects.isEmpty()) {
            return false;
        }
        List<String> projectIds = projects.stream().map(MrpProject::getId).collect(Collectors.toList());
        Long count = acceptanceOverdueMapper.selectCount(new QueryWrapper<MrpAcceptanceOverdue>()
                .in("project_id", projectIds).in("status", Arrays.asList(0, 1, 2)));
        return count != null && count > 0;
    }

    private MrpApplicationConflictCheck buildCheck(MrpApplication application, String type, boolean pass,
                                                   String passDetail, String failDetail, Date now) {
        MrpApplicationConflictCheck check = new MrpApplicationConflictCheck();
        check.setApplicationId(application.getId());
        check.setApplicantId(application.getApplicantId());
        check.setCheckType(type);
        check.setCheckResult(pass ? 1 : 0);
        check.setCheckDetail(pass ? passDetail : failDetail);
        check.setCheckTime(now);
        check.setChecker("系统");
        return check;
    }

    private String buildSnapshotJson(MrpApplication application) {
        JSONObject json = new JSONObject();
        json.put("guideId", application.getGuideId());
        json.put("guideTitle", application.getGuideTitle());
        json.put("applicantId", application.getApplicantId());
        json.put("applicantName", application.getApplicantName());
        json.put("applicantOrg", application.getApplicantOrg());
        json.put("applicantTitle", application.getApplicantTitle());
        json.put("projectType", application.getProjectType());
        json.put("researchField", application.getResearchField());
        json.put("projectTitle", application.getProjectTitle());
        json.put("projectSummary", application.getProjectSummary());
        json.put("budget", application.getBudget());
        json.put("durationMonths", application.getDurationMonths());
        return json.toJSONString();
    }

    private MrpApplication getApplicationOrThrow(String id) {
        MrpApplication application = getById(id);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        return application;
    }

    private void checkStatus(MrpApplication application, ApplicationStatusEnum expect, String message) {
        if (!expect.getCode().equals(application.getStatus())) {
            throw new ProjectBizException(message);
        }
    }
}

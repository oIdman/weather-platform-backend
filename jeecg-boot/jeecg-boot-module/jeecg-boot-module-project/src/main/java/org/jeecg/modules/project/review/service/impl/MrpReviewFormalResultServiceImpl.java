package org.jeecg.modules.project.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRectification;
import org.jeecg.modules.project.review.entity.MrpReviewFormalResult;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRule;
import org.jeecg.modules.project.review.mapper.MrpReviewFormalResultMapper;
import org.jeecg.modules.project.review.service.IMrpReviewFormalRectificationService;
import org.jeecg.modules.project.review.service.IMrpReviewFormalResultService;
import org.jeecg.modules.project.review.service.IMrpReviewFormalRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 形式审查结果 Service 实现（自动化预审：规则引擎，非 AI）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpReviewFormalResultServiceImpl extends ServiceImpl<MrpReviewFormalResultMapper, MrpReviewFormalResult> implements IMrpReviewFormalResultService {

    @Autowired
    private IMrpReviewFormalRuleService formalRuleService;

    @Autowired
    private IMrpReviewFormalRectificationService rectificationService;

    @Autowired
    private IMrpApplicationService applicationService;

    @Override
    public List<MrpReviewFormalResult> runFormalReview(String applicationId) {
        MrpApplication application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        if (!isReviewable(application.getStatus())) {
            throw new ProjectBizException("当前申报状态不允许执行形式审查");
        }
        List<MrpReviewFormalRule> rules = formalRuleService.list(
                new QueryWrapper<MrpReviewFormalRule>().eq("is_enabled", 1).orderByAsc("sort_order"));
        if (rules.isEmpty()) {
            throw new ProjectBizException("未配置启用的形式审查规则");
        }
        Date now = new Date();
        List<MrpReviewFormalResult> results = new ArrayList<>();
        for (MrpReviewFormalRule rule : rules) {
            CheckResult check = doCheck(rule, application);
            MrpReviewFormalResult result = new MrpReviewFormalResult();
            result.setApplicationId(applicationId);
            result.setRuleId(rule.getId());
            result.setRuleName(rule.getRuleName());
            result.setCheckResult(check.pass ? 1 : 0);
            result.setCheckDetail(check.detail);
            result.setCheckTime(now);
            result.setChecker("系统");
            results.add(result);
        }
        saveBatch(results);
        application.setStatus(ApplicationStatusEnum.FORMAL_REVIEW.getCode());
        boolean allPass = results.stream().allMatch(r -> Integer.valueOf(1).equals(r.getCheckResult()));
        if (allPass) {
            application.setStatus(ApplicationStatusEnum.EXPERT_REVIEW.getCode());
            applicationService.updateById(application);
        } else {
            String issueDesc = results.stream()
                    .filter(r -> !Integer.valueOf(1).equals(r.getCheckResult()))
                    .map(r -> "【" + r.getRuleName() + "】" + r.getCheckDetail())
                    .collect(Collectors.joining("；"));
            application.setStatus(ApplicationStatusEnum.RECTIFYING.getCode());
            applicationService.updateById(application);
            rectificationService.createRectification(applicationId, issueDesc);
        }
        return results;
    }

    private boolean isReviewable(Integer status) {
        return ApplicationStatusEnum.SUBMITTED.getCode().equals(status)
                || ApplicationStatusEnum.FORMAL_REVIEW.getCode().equals(status)
                || ApplicationStatusEnum.RECTIFYING.getCode().equals(status);
    }

    private CheckResult doCheck(MrpReviewFormalRule rule, MrpApplication application) {
        String type = rule.getRuleType();
        List<String> problems = new ArrayList<>();
        if ("completeness".equals(type)) {
            if (oConvertUtils.isEmpty(application.getProjectTitle())) {
                problems.add("缺少项目名称");
            }
            if (oConvertUtils.isEmpty(application.getProjectSummary())) {
                problems.add("缺少研究方案");
            }
            if (oConvertUtils.isEmpty(application.getApplicantName())) {
                problems.add("缺少申请人");
            }
        } else if ("format".equals(type)) {
            if (application.getBudget() == null || application.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
                problems.add("申请经费未填写或非法");
            }
            if (application.getDurationMonths() == null || application.getDurationMonths() <= 0) {
                problems.add("计划周期未填写或非法");
            }
        } else if ("qualification".equals(type)) {
            if (oConvertUtils.isEmpty(application.getApplicantTitle())) {
                problems.add("申请人职称信息缺失");
            }
        } else {
            return new CheckResult(true, "规则类型未启用内置校验：" + type);
        }
        return problems.isEmpty()
                ? new CheckResult(true, "校验通过")
                : new CheckResult(false, String.join("；", problems));
    }

    private static class CheckResult {
        private final boolean pass;
        private final String detail;

        private CheckResult(boolean pass, String detail) {
            this.pass = pass;
            this.detail = detail;
        }
    }
}

package org.jeecg.modules.project.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.expert.entity.MrpExpert;
import org.jeecg.modules.project.expert.service.IMrpExpertService;
import org.jeecg.modules.project.review.entity.MrpReviewExpertAssignment;
import org.jeecg.modules.project.review.mapper.MrpReviewExpertAssignmentMapper;
import org.jeecg.modules.project.review.service.IMrpReviewExpertAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: 专家分配 Service 实现（领域匹配 + 星级加权 + 回避规则 + 随机抽取）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpReviewExpertAssignmentServiceImpl extends ServiceImpl<MrpReviewExpertAssignmentMapper, MrpReviewExpertAssignment> implements IMrpReviewExpertAssignmentService {

    @Autowired
    private IMrpExpertService expertService;

    @Autowired
    private IMrpApplicationService applicationService;

    private final Random random = new Random();

    @Override
    public List<MrpReviewExpertAssignment> selectExperts(String applicationId, int expertCount) {
        MrpApplication application = getReviewableApplication(applicationId);
        if (oConvertUtils.isEmpty(application.getResearchField())) {
            throw new ProjectBizException("申报未填写研究方向，无法自动遴选，请人工分配");
        }
        List<MrpExpert> candidates = expertService.list(
                new QueryWrapper<MrpExpert>().like("research_field", application.getResearchField()));
        if (candidates.isEmpty()) {
            throw new ProjectBizException("未找到研究方向匹配的专家，请人工分配");
        }
        // 回避规则：专家回避单位与申请人机构相同则剔除
        String applicantOrg = application.getApplicantOrg();
        if (oConvertUtils.isNotEmpty(applicantOrg)) {
            candidates = candidates.stream()
                    .filter(e -> !applicantOrg.equals(e.getAvoidOrg()))
                    .collect(Collectors.toList());
        }
        // 剔除已分配过的专家，避免重复
        Set<String> assignedExpertIds = list(new QueryWrapper<MrpReviewExpertAssignment>()
                .eq("application_id", applicationId)).stream()
                .map(MrpReviewExpertAssignment::getExpertId)
                .collect(Collectors.toSet());
        candidates = candidates.stream()
                .filter(e -> !assignedExpertIds.contains(e.getId()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            throw new ProjectBizException("匹配专家均已被分配或命中回避，请人工分配");
        }
        // 星级加权 + 随机抽取
        candidates.sort((a, b) -> Double.compare(weight(b), weight(a)));
        int take = Math.min(expertCount <= 0 ? 3 : expertCount, candidates.size());
        candidates = candidates.subList(0, take);
        int batch = nextBatch(applicationId);
        Date now = new Date();
        List<MrpReviewExpertAssignment> assignments = new ArrayList<>();
        for (MrpExpert expert : candidates) {
            MrpReviewExpertAssignment assignment = new MrpReviewExpertAssignment();
            assignment.setApplicationId(applicationId);
            assignment.setExpertId(expert.getId());
            assignment.setExpertName(expert.getName());
            assignment.setExpertField(expert.getResearchField());
            assignment.setAssignBatch(batch);
            assignment.setAssignWay("auto_random");
            assignment.setAvoidChecked(1);
            assignment.setAvoidResult(1);
            assignment.setStatus(0);
            assignment.setAssignedTime(now);
            assignments.add(assignment);
        }
        saveBatch(assignments);
        return assignments;
    }

    @Override
    public MrpReviewExpertAssignment manualAssign(MrpReviewExpertAssignment assignment) {
        getReviewableApplication(assignment.getApplicationId());
        MrpExpert expert = expertService.getById(assignment.getExpertId());
        if (expert == null) {
            throw new ProjectBizException("专家不存在");
        }
        if (oConvertUtils.isNotEmpty(assignment.getApplicationId())
                && oConvertUtils.isNotEmpty(assignment.getExpertId())) {
            long exists = count(new QueryWrapper<MrpReviewExpertAssignment>()
                    .eq("application_id", assignment.getApplicationId())
                    .eq("expert_id", assignment.getExpertId()));
            if (exists > 0) {
                throw new ProjectBizException("该专家已分配过此申报");
            }
        }
        assignment.setExpertName(expert.getName());
        assignment.setExpertField(expert.getResearchField());
        assignment.setAssignBatch(nextBatch(assignment.getApplicationId()));
        assignment.setAssignWay("manual");
        assignment.setAvoidChecked(1);
        assignment.setAvoidResult(1);
        assignment.setStatus(0);
        assignment.setAssignedTime(new Date());
        save(assignment);
        return assignment;
    }

    @Override
    public void completeReview(String applicationId, boolean pass, String conclusion) {
        MrpApplication application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        if (!ApplicationStatusEnum.EXPERT_REVIEW.getCode().equals(application.getStatus())) {
            throw new ProjectBizException("仅专家评审中状态可完成评审");
        }
        application.setStatus(pass
                ? ApplicationStatusEnum.APPROVED.getCode()
                : ApplicationStatusEnum.REJECTED.getCode());
        application.setRemark(conclusion);
        applicationService.updateById(application);
    }

    private MrpApplication getReviewableApplication(String applicationId) {
        MrpApplication application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        if (!ApplicationStatusEnum.EXPERT_REVIEW.getCode().equals(application.getStatus())) {
            throw new ProjectBizException("仅专家评审中状态可分配专家");
        }
        return application;
    }

    private double weight(MrpExpert expert) {
        int star = expert.getStarLevel() == null ? 0 : expert.getStarLevel();
        return star + random.nextDouble();
    }

    private int nextBatch(String applicationId) {
        Integer max = list(new QueryWrapper<MrpReviewExpertAssignment>()
                .eq("application_id", applicationId).select("assign_batch")).stream()
                .map(MrpReviewExpertAssignment::getAssignBatch)
                .filter(b -> b != null)
                .max(Integer::compareTo)
                .orElse(0);
        return max + 1;
    }
}

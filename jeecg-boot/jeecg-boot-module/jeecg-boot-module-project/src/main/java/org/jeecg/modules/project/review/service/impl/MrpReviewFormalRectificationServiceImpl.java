package org.jeecg.modules.project.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRectification;
import org.jeecg.modules.project.review.mapper.MrpReviewFormalRectificationMapper;
import org.jeecg.modules.project.review.service.IMrpReviewFormalRectificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 整改重报 Service 实现（待整改 → 已整改重报 → 复核）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpReviewFormalRectificationServiceImpl extends ServiceImpl<MrpReviewFormalRectificationMapper, MrpReviewFormalRectification> implements IMrpReviewFormalRectificationService {

    @Autowired
    private IMrpApplicationService applicationService;

    @Override
    public MrpReviewFormalRectification createRectification(String applicationId, String issueDesc) {
        MrpReviewFormalRectification rectification = new MrpReviewFormalRectification();
        rectification.setApplicationId(applicationId);
        rectification.setRectificationNo("GZ" + System.currentTimeMillis());
        rectification.setIssueDesc(issueDesc);
        rectification.setRectificationStatus(0);
        rectification.setDeadline(new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
        save(rectification);
        return rectification;
    }

    @Override
    public void rectify(String id, String rectifyContent) {
        MrpReviewFormalRectification rectification = getRectificationOrThrow(id);
        if (!Integer.valueOf(0).equals(rectification.getRectificationStatus())) {
            throw new ProjectBizException("仅待整改状态可重报");
        }
        rectification.setRectificationStatus(1);
        rectification.setRectifyContent(rectifyContent);
        rectification.setRectifyTime(new Date());
        updateById(rectification);
        MrpApplication application = applicationService.getById(rectification.getApplicationId());
        if (application != null) {
            application.setStatus(ApplicationStatusEnum.FORMAL_REVIEW.getCode());
            applicationService.updateById(application);
        }
    }

    @Override
    public void reviewRectification(String id, boolean pass, String reviewResult) {
        MrpReviewFormalRectification rectification = getRectificationOrThrow(id);
        if (!Integer.valueOf(1).equals(rectification.getRectificationStatus())) {
            throw new ProjectBizException("仅已整改重报状态可复核");
        }
        rectification.setRectificationStatus(pass ? 2 : 3);
        rectification.setReviewResult(reviewResult);
        rectification.setReviewer("admin");
        rectification.setReviewTime(new Date());
        updateById(rectification);
        MrpApplication application = applicationService.getById(rectification.getApplicationId());
        if (application != null) {
            application.setStatus(pass
                    ? ApplicationStatusEnum.EXPERT_REVIEW.getCode()
                    : ApplicationStatusEnum.RECTIFYING.getCode());
            applicationService.updateById(application);
        }
    }

    private MrpReviewFormalRectification getRectificationOrThrow(String id) {
        MrpReviewFormalRectification rectification = getById(id);
        if (rectification == null) {
            throw new ProjectBizException("整改记录不存在");
        }
        return rectification;
    }
}

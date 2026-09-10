package org.jeecg.modules.project.acceptance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceApplication;
import org.jeecg.modules.project.acceptance.mapper.MrpAcceptanceApplicationMapper;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceApplicationService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 验收申请 Service 实现（提交与初审）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Service
public class MrpAcceptanceApplicationServiceImpl extends ServiceImpl<MrpAcceptanceApplicationMapper, MrpAcceptanceApplication> implements IMrpAcceptanceApplicationService {

    @Override
    public void submit(String id) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(0).equals(application.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交");
        }
        application.setApplicationNo("YS" + System.currentTimeMillis());
        application.setStatus(1);
        application.setApplyBy("admin");
        application.setApplyTime(new Date());
        updateById(application);
    }

    @Override
    public void preReview(String id, boolean pass, String opinion) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(1).equals(application.getStatus())) {
            throw new ProjectBizException("仅已提交状态可初审");
        }
        application.setStatus(pass ? 3 : 1);
        application.setPreReviewer("admin");
        application.setPreReviewOpinion(opinion);
        application.setPreReviewTime(new Date());
        updateById(application);
    }

    private MrpAcceptanceApplication getApplicationOrThrow(String id) {
        MrpAcceptanceApplication application = getById(id);
        if (application == null) {
            throw new ProjectBizException("验收申请不存在");
        }
        return application;
    }
}

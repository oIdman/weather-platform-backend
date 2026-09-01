package org.jeecg.modules.project.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;
import org.jeecg.modules.project.approval.mapper.MrpApprovalObjectionMapper;
import org.jeecg.modules.project.approval.mapper.MrpApprovalPublicityMapper;
import org.jeecg.modules.project.approval.service.IMrpApprovalPublicityService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: 立项公示 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpApprovalPublicityServiceImpl extends ServiceImpl<MrpApprovalPublicityMapper, MrpApprovalPublicity> implements IMrpApprovalPublicityService {

    @Autowired
    private IMrpApplicationService applicationService;

    @Autowired
    private MrpApprovalObjectionMapper objectionMapper;

    @Override
    public MrpApprovalPublicity start(String applicationId, Date endTime) {
        MrpApplication application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        if (!ApplicationStatusEnum.APPROVED.getCode().equals(application.getStatus())) {
            throw new ProjectBizException("仅评审通过的申报可发起公示");
        }
        long exists = count(new QueryWrapper<MrpApprovalPublicity>()
                .eq("application_id", applicationId).eq("status", 0));
        if (exists > 0) {
            throw new ProjectBizException("该申报已有进行中的公示");
        }
        MrpApprovalPublicity publicity = new MrpApprovalPublicity();
        publicity.setPublicityNo("GS" + System.currentTimeMillis());
        publicity.setApplicationId(applicationId);
        publicity.setProjectName(application.getProjectTitle());
        publicity.setStartTime(new Date());
        publicity.setEndTime(endTime == null ? new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000) : endTime);
        publicity.setStatus(0);
        publicity.setResult(0);
        save(publicity);
        return publicity;
    }

    @Override
    public void finish(String publicityId) {
        MrpApprovalPublicity publicity = getPublicityOrThrow(publicityId);
        if (!Integer.valueOf(0).equals(publicity.getStatus())) {
            throw new ProjectBizException("仅公示中状态可结束公示");
        }
        List<MrpApprovalObjection> objections = objectionMapper.selectList(
                new QueryWrapper<MrpApprovalObjection>().eq("publicity_id", publicityId));
        boolean hasEstablished = objections.stream()
                .anyMatch(o -> Integer.valueOf(2).equals(o.getStatus()));
        publicity.setStatus(1);
        publicity.setResult(hasEstablished ? 2 : 0);
        updateById(publicity);
    }

    @Override
    public void markObjectionProcessing(String publicityId) {
        MrpApprovalPublicity publicity = getPublicityOrThrow(publicityId);
        publicity.setResult(1);
        updateById(publicity);
    }

    @Override
    public void setResult(String publicityId, Integer result) {
        MrpApprovalPublicity publicity = getPublicityOrThrow(publicityId);
        publicity.setResult(result);
        updateById(publicity);
    }

    private MrpApprovalPublicity getPublicityOrThrow(String publicityId) {
        MrpApprovalPublicity publicity = getById(publicityId);
        if (publicity == null) {
            throw new ProjectBizException("公示记录不存在");
        }
        return publicity;
    }
}

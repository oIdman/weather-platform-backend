package org.jeecg.modules.project.approval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;
import org.jeecg.modules.project.approval.mapper.MrpApprovalObjectionMapper;
import org.jeecg.modules.project.approval.service.IMrpApprovalObjectionService;
import org.jeecg.modules.project.approval.service.IMrpApprovalPublicityService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Description: 立项异议 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpApprovalObjectionServiceImpl extends ServiceImpl<MrpApprovalObjectionMapper, MrpApprovalObjection> implements IMrpApprovalObjectionService {

    @Autowired
    private IMrpApprovalPublicityService publicityService;

    @Override
    public MrpApprovalObjection submit(MrpApprovalObjection objection) {
        if (objection.getPublicityId() == null || objection.getObjectionContent() == null) {
            throw new ProjectBizException("公示ID与异议内容不能为空");
        }
        MrpApprovalPublicity publicity = publicityService.getById(objection.getPublicityId());
        if (publicity == null) {
            throw new ProjectBizException("公示记录不存在");
        }
        if (!Integer.valueOf(0).equals(publicity.getStatus())) {
            throw new ProjectBizException("公示已结束，不再受理异议");
        }
        objection.setObjectionNo("YY" + System.currentTimeMillis());
        objection.setApplicationId(publicity.getApplicationId());
        objection.setStatus(0);
        save(objection);
        return objection;
    }
}

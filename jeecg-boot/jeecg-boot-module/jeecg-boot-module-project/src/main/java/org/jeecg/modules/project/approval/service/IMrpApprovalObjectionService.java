package org.jeecg.modules.project.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;

/**
 * @Description: 立项异议 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpApprovalObjectionService extends IService<MrpApprovalObjection> {

    /** 登记异议 */
    MrpApprovalObjection submit(MrpApprovalObjection objection);
}

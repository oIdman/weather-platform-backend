package org.jeecg.modules.project.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;

import java.util.Date;

/**
 * @Description: 立项公示 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpApprovalPublicityService extends IService<MrpApprovalPublicity> {

    /** 发起公示（评审通过的申报） */
    MrpApprovalPublicity start(String applicationId, Date endTime);

    /** 结束公示（有成立异议则结论为 2，否则 0） */
    void finish(String publicityId);

    /** 异议提交时置公示结论为"异议处理中" */
    void markObjectionProcessing(String publicityId);

    /** 更新公示结论（0-无异议,1-异议处理中,2-有成立异议） */
    void setResult(String publicityId, Integer result);
}

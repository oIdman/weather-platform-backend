package org.jeecg.modules.project.acceptance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceApplication;

/**
 * @Description: 验收申请 Service
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
public interface IMrpAcceptanceApplicationService extends IService<MrpAcceptanceApplication> {

    /** 提交：草稿 → 已提交 */
    void submit(String id);

    /** 初审：通过 → 待专家评审；不通过 → 退回已提交 */
    void preReview(String id, boolean pass, String opinion);

}

package org.jeecg.modules.project.execution.midcheck.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReview;

/**
 * @Description: 中检评审 Service（Flowable 中检评审流程）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpMidcheckReviewService extends IService<MrpMidcheckReview> {

    /** 发起评审：已提交 → 评审中（启动 Flowable 流程） */
    MrpMidcheckReview startReview(String midcheckReportId);

    /** 完成评审：通过/退回，退回自动生成整改任务 */
    void completeReview(String midcheckReportId, boolean pass, String opinion);
}

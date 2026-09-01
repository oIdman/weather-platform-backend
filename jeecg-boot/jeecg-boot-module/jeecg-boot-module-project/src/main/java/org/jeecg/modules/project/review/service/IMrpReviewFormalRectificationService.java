package org.jeecg.modules.project.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRectification;

/**
 * @Description: 整改重报 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpReviewFormalRectificationService extends IService<MrpReviewFormalRectification> {

    /** 生成整改单（预审未通过时调用） */
    MrpReviewFormalRectification createRectification(String applicationId, String issueDesc);

    /** 申报人整改重报：待整改 → 已整改重报（申报回到待复核） */
    void rectify(String id, String rectifyContent);

    /** 复核整改：通过/不通过，并联动申报状态 */
    void reviewRectification(String id, boolean pass, String reviewResult);
}

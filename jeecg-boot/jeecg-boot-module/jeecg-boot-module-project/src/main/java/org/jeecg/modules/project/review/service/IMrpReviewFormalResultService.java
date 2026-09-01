package org.jeecg.modules.project.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.review.entity.MrpReviewFormalResult;

import java.util.List;

/**
 * @Description: 形式审查结果 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpReviewFormalResultService extends IService<MrpReviewFormalResult> {

    /** 运行自动化预审（按启用规则逐条校验，落库结果并按结果流转申报状态） */
    List<MrpReviewFormalResult> runFormalReview(String applicationId);
}

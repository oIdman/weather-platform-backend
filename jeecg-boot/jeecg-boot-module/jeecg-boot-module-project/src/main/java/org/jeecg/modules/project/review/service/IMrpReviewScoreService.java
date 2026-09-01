package org.jeecg.modules.project.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.review.entity.MrpReviewScore;
import org.jeecg.modules.project.review.vo.MrpReviewScoreSubmitVO;

/**
 * @Description: 专家打分 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpReviewScoreService extends IService<MrpReviewScore> {

    /** 提交专家评审（多维度打分 + 总体意见，分配置为已完成） */
    int submitScores(MrpReviewScoreSubmitVO vo);
}

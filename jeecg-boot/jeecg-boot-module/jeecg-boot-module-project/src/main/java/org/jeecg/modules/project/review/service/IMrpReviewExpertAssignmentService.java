package org.jeecg.modules.project.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.review.entity.MrpReviewExpertAssignment;

import java.util.List;

/**
 * @Description: 专家分配 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpReviewExpertAssignmentService extends IService<MrpReviewExpertAssignment> {

    /** 智能专家遴选：领域匹配 + 星级加权 + 回避规则 + 随机抽取 */
    List<MrpReviewExpertAssignment> selectExperts(String applicationId, int expertCount);

    /** 人工分配专家 */
    MrpReviewExpertAssignment manualAssign(MrpReviewExpertAssignment assignment);

    /** 完成评审：按汇总结论流转申报状态（评审通过/已拒绝） */
    void completeReview(String applicationId, boolean pass, String conclusion);
}

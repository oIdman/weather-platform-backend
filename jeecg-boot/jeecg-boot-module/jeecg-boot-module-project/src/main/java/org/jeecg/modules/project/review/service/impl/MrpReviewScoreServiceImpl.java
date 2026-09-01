package org.jeecg.modules.project.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.review.entity.MrpReviewExpertAssignment;
import org.jeecg.modules.project.review.entity.MrpReviewOpinion;
import org.jeecg.modules.project.review.entity.MrpReviewScore;
import org.jeecg.modules.project.review.mapper.MrpReviewScoreMapper;
import org.jeecg.modules.project.review.service.IMrpReviewExpertAssignmentService;
import org.jeecg.modules.project.review.service.IMrpReviewOpinionService;
import org.jeecg.modules.project.review.service.IMrpReviewScoreService;
import org.jeecg.modules.project.review.vo.MrpReviewScoreSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 专家打分 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpReviewScoreServiceImpl extends ServiceImpl<MrpReviewScoreMapper, MrpReviewScore> implements IMrpReviewScoreService {

    @Autowired
    private IMrpReviewExpertAssignmentService assignmentService;

    @Autowired
    private IMrpReviewOpinionService opinionService;

    @Override
    public int submitScores(MrpReviewScoreSubmitVO vo) {
        if (vo.getAssignmentId() == null || vo.getItems() == null || vo.getItems().isEmpty()) {
            throw new ProjectBizException("分配ID与打分项不能为空");
        }
        MrpReviewExpertAssignment assignment = assignmentService.getById(vo.getAssignmentId());
        if (assignment == null) {
            throw new ProjectBizException("专家分配记录不存在");
        }
        Date endTime = vo.getReviewEndTime() == null ? new Date() : vo.getReviewEndTime();
        List<MrpReviewScore> scores = new ArrayList<>();
        for (MrpReviewScoreSubmitVO.Item item : vo.getItems()) {
            MrpReviewScore score = new MrpReviewScore();
            score.setApplicationId(assignment.getApplicationId());
            score.setAssignmentId(assignment.getId());
            score.setExpertId(assignment.getExpertId());
            score.setExpertName(assignment.getExpertName());
            score.setDimension(item.getDimension());
            score.setScore(item.getScore());
            score.setReviewStartTime(vo.getReviewStartTime());
            score.setReviewEndTime(endTime);
            scores.add(score);
        }
        saveBatch(scores);
        if (vo.getOpinion() != null && !vo.getOpinion().isEmpty()) {
            MrpReviewOpinion opinion = new MrpReviewOpinion();
            opinion.setApplicationId(assignment.getApplicationId());
            opinion.setAssignmentId(assignment.getId());
            opinion.setExpertId(assignment.getExpertId());
            opinion.setExpertName(assignment.getExpertName());
            opinion.setOpinionType("overall");
            opinion.setOpinionContent(vo.getOpinion());
            opinionService.save(opinion);
        }
        assignment.setStatus(2);
        assignment.setCompletedTime(endTime);
        assignmentService.updateById(assignment);
        return scores.size();
    }
}

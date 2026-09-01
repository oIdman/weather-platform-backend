package org.jeecg.modules.project.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.review.entity.MrpReviewExpertAssignment;
import org.jeecg.modules.project.review.entity.MrpReviewOpinion;
import org.jeecg.modules.project.review.entity.MrpReviewScore;
import org.jeecg.modules.project.review.service.IMrpReviewExpertAssignmentService;
import org.jeecg.modules.project.review.service.IMrpReviewOpinionService;
import org.jeecg.modules.project.review.service.IMrpReviewScoreService;
import org.jeecg.modules.project.review.vo.MrpReviewScoreSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 专家评审（智能遴选 / 在线打分 / 意见 / 汇总，AI 分析占位）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "申报专家评审")
@RestController
@RequestMapping("/project/review/expert")
@Slf4j
public class MrpReviewExpertController {

    @Autowired
    private IMrpReviewExpertAssignmentService assignmentService;

    @Autowired
    private IMrpReviewScoreService scoreService;

    @Autowired
    private IMrpReviewOpinionService opinionService;

    @Operation(summary = "智能专家遴选", description = "领域匹配 + 星级加权 + 回避规则 + 随机抽取")
    @PostMapping(value = "/select")
    public Result<List<MrpReviewExpertAssignment>> select(@RequestBody Map<String, Object> body) {
        String applicationId = (String) body.get("applicationId");
        int expertCount = body.get("expertCount") == null ? 3 : Integer.parseInt(String.valueOf(body.get("expertCount")));
        if (applicationId == null) {
            return Result.error("参数不完整！");
        }
        List<MrpReviewExpertAssignment> assignments = assignmentService.selectExperts(applicationId, expertCount);
        return Result.ok(assignments);
    }

    @Operation(summary = "人工分配专家", description = "人工指定专家评审")
    @PostMapping(value = "/manualAssign")
    public Result<MrpReviewExpertAssignment> manualAssign(@RequestBody MrpReviewExpertAssignment assignment) {
        if (assignment.getApplicationId() == null || assignment.getExpertId() == null) {
            return Result.error("申报ID与专家ID不能为空！");
        }
        MrpReviewExpertAssignment saved = assignmentService.manualAssign(assignment);
        return Result.OK("分配成功！", saved);
    }

    @Operation(summary = "提交专家评审", description = "多维度打分 + 总体意见，完成后分配置为已完成")
    @PostMapping(value = "/submitScore")
    public Result<Integer> submitScore(@RequestBody MrpReviewScoreSubmitVO vo) {
        int count = scoreService.submitScores(vo);
        return Result.OK("评审提交成功！", count);
    }

    @Operation(summary = "完成评审结论", description = "按汇总结果流转申报状态（评审通过/已拒绝）")
    @PostMapping(value = "/complete")
    public Result<Void> complete(@RequestBody Map<String, Object> body) {
        String applicationId = (String) body.get("applicationId");
        Boolean pass = (Boolean) body.get("pass");
        String conclusion = (String) body.get("conclusion");
        if (applicationId == null || pass == null) {
            return Result.error("参数不完整！");
        }
        assignmentService.completeReview(applicationId, pass, conclusion);
        return Result.ok("评审完成！");
    }

    @Operation(summary = "专家分配查询", description = "按申报ID查询专家分配列表")
    @GetMapping(value = "/assignments")
    public Result<List<MrpReviewExpertAssignment>> assignments(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewExpertAssignment> list = assignmentService.list(
                new QueryWrapper<MrpReviewExpertAssignment>().eq("application_id", applicationId).orderByAsc("assign_batch"));
        return Result.ok(list);
    }

    @Operation(summary = "专家打分查询", description = "按申报ID查询打分记录")
    @GetMapping(value = "/scores")
    public Result<List<MrpReviewScore>> scores(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewScore> list = scoreService.list(
                new QueryWrapper<MrpReviewScore>().eq("application_id", applicationId).orderByAsc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "专家意见查询", description = "按申报ID查询评审意见")
    @GetMapping(value = "/opinions")
    public Result<List<MrpReviewOpinion>> opinions(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewOpinion> list = opinionService.list(
                new QueryWrapper<MrpReviewOpinion>().eq("application_id", applicationId).orderByAsc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "评审汇总", description = "专家/打分/意见 + 各维度均分与总分")
    @GetMapping(value = "/summary")
    public Result<Map<String, Object>> summary(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewExpertAssignment> assignments = assignmentService.list(
                new QueryWrapper<MrpReviewExpertAssignment>().eq("application_id", applicationId));
        List<MrpReviewScore> scores = scoreService.list(
                new QueryWrapper<MrpReviewScore>().eq("application_id", applicationId));
        List<MrpReviewOpinion> opinions = opinionService.list(
                new QueryWrapper<MrpReviewOpinion>().eq("application_id", applicationId));
        Map<String, Double> dimensionAverages = scores.stream()
                .collect(Collectors.groupingBy(MrpReviewScore::getDimension,
                        Collectors.averagingDouble(s -> s.getScore() == null ? 0 : s.getScore().doubleValue())));
        double totalScore = dimensionAverages.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Object> data = new HashMap<>(8);
        data.put("assignments", assignments);
        data.put("scores", scores);
        data.put("opinions", opinions);
        data.put("dimensionAverages", dimensionAverages);
        data.put("totalScore", Math.round(totalScore * 10) / 10.0);
        return Result.ok(data);
    }

    @Operation(summary = "评审意见智能分析（AI 占位）", description = "意见聚类/情感分析为二期 AI 能力，当前为占位接口")
    @PostMapping(value = "/aiAnalyzeOpinions")
    public Result<Map<String, String>> aiAnalyzeOpinions(@RequestParam(name = "applicationId", required = true) String applicationId) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "评审意见智能分析（主题聚类、情感分析）为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }
}

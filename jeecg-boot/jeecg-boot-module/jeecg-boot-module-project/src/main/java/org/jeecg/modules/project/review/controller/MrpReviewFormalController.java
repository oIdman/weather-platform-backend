package org.jeecg.modules.project.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.review.entity.MrpReviewFormalRectification;
import org.jeecg.modules.project.review.entity.MrpReviewFormalResult;
import org.jeecg.modules.project.review.service.IMrpReviewFormalRectificationService;
import org.jeecg.modules.project.review.service.IMrpReviewFormalResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description: 形式审查（自动化预审 + 整改重报闭环）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "申报形式审查")
@RestController
@RequestMapping("/project/review/formal")
@Slf4j
public class MrpReviewFormalController {

    @Autowired
    private IMrpReviewFormalResultService formalResultService;

    @Autowired
    private IMrpReviewFormalRectificationService rectificationService;

    @Operation(summary = "运行自动化预审", description = "按启用规则逐条校验，未通过自动生成整改单并流转申报状态")
    @PostMapping(value = "/run")
    public Result<List<MrpReviewFormalResult>> run(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewFormalResult> results = formalResultService.runFormalReview(applicationId);
        return Result.ok(results);
    }

    @Operation(summary = "形式审查结果查询", description = "按申报ID查询预审结果")
    @GetMapping(value = "/result")
    public Result<List<MrpReviewFormalResult>> result(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewFormalResult> list = formalResultService.list(
                new QueryWrapper<MrpReviewFormalResult>().eq("application_id", applicationId).orderByAsc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "整改单查询", description = "按申报ID查询整改记录")
    @GetMapping(value = "/rectificationList")
    public Result<List<MrpReviewFormalRectification>> rectificationList(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpReviewFormalRectification> list = rectificationService.list(
                new QueryWrapper<MrpReviewFormalRectification>().eq("application_id", applicationId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "整改重报", description = "待整改 → 已整改重报，申报回到待复核")
    @PostMapping(value = "/rectify")
    public Result<MrpReviewFormalRectification> rectify(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String rectifyContent = body.get("rectifyContent");
        if (id == null || rectifyContent == null) {
            return Result.error("参数不完整！");
        }
        rectificationService.rectify(id, rectifyContent);
        return Result.ok("重报成功！");
    }

    @Operation(summary = "整改复核", description = "复核通过进入专家评审，不通过继续整改")
    @PostMapping(value = "/reviewRectification")
    public Result<MrpReviewFormalRectification> reviewRectification(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        Boolean pass = (Boolean) body.get("pass");
        String reviewResult = (String) body.get("reviewResult");
        if (id == null || pass == null) {
            return Result.error("参数不完整！");
        }
        rectificationService.reviewRectification(id, pass, reviewResult);
        return Result.ok("复核完成！");
    }
}

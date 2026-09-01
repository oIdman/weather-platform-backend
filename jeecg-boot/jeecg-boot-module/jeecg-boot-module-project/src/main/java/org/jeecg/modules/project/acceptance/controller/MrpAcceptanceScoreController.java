package org.jeecg.modules.project.acceptance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceScore;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 验收打分（任务完成度 / 成果质量 / 汇报答辩）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Tag(name = "验收打分")
@RestController
@RequestMapping("/project/acceptance/score")
@Slf4j
public class MrpAcceptanceScoreController {

    @Autowired
    private IMrpAcceptanceScoreService scoreService;

    @Operation(summary = "打分列表查询", description = "可按验收申请/项目过滤")
    @GetMapping(value = "/list")
    public Result<List<MrpAcceptanceScore>> list(@RequestParam(name = "acceptanceApplicationId", required = false) String acceptanceApplicationId,
                                                 @RequestParam(name = "projectId", required = false) String projectId) {
        QueryWrapper<MrpAcceptanceScore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(acceptanceApplicationId != null, "acceptance_application_id", acceptanceApplicationId)
                .eq(projectId != null, "project_id", projectId)
                .orderByAsc("create_time");
        return Result.ok(scoreService.list(queryWrapper));
    }

    @Operation(summary = "打分添加", description = "按验收申请录入单维度打分")
    @PostMapping(value = "/add")
    public Result<MrpAcceptanceScore> add(@RequestBody MrpAcceptanceScore score) {
        scoreService.save(score);
        return Result.ok("打分成功！");
    }

    @Operation(summary = "打分编辑", description = "打分编辑")
    @PutMapping(value = "/edit")
    public Result<MrpAcceptanceScore> edit(@RequestBody MrpAcceptanceScore score) {
        MrpAcceptanceScore byId = scoreService.getById(score.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        scoreService.updateById(score);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "打分通过id删除", description = "打分通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpAcceptanceScore> delete(@RequestParam(name = "id", required = true) String id) {
        MrpAcceptanceScore byId = scoreService.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        scoreService.removeById(id);
        return Result.ok("删除成功！");
    }
}

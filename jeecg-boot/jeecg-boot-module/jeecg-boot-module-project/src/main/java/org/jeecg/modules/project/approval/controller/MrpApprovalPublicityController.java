package org.jeecg.modules.project.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;
import org.jeecg.modules.project.approval.service.IMrpApprovalPublicityService;
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
 * @Description: 立项公示管理
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "立项公示")
@RestController
@RequestMapping("/project/approval/publicity")
@Slf4j
public class MrpApprovalPublicityController {

    @Autowired
    private IMrpApprovalPublicityService publicityService;

    @Operation(summary = "公示列表查询", description = "可按申报ID/状态过滤")
    @GetMapping(value = "/list")
    public Result<List<MrpApprovalPublicity>> list(@RequestParam(name = "applicationId", required = false) String applicationId,
                                                   @RequestParam(name = "status", required = false) Integer status) {
        QueryWrapper<MrpApprovalPublicity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(applicationId != null, "application_id", applicationId)
                .eq(status != null, "status", status)
                .orderByDesc("create_time");
        return Result.ok(publicityService.list(queryWrapper));
    }

    @Operation(summary = "发起公示", description = "评审通过的申报发起公示，默认公示期 7 天")
    @PostMapping(value = "/start")
    public Result<MrpApprovalPublicity> start(@RequestBody Map<String, Object> body) {
        String applicationId = (String) body.get("applicationId");
        if (applicationId == null) {
            return Result.error("参数不完整！");
        }
        MrpApprovalPublicity publicity = publicityService.start(applicationId, null);
        return Result.OK("公示发起成功！", publicity);
    }

    @Operation(summary = "结束公示", description = "有成立异议则结论为有异议，否则无异议")
    @PostMapping(value = "/finish")
    public Result<MrpApprovalPublicity> finish(@RequestBody Map<String, String> body) {
        String publicityId = body.get("publicityId");
        if (publicityId == null) {
            return Result.error("参数不完整！");
        }
        publicityService.finish(publicityId);
        return Result.ok("公示结束！");
    }

    @Operation(summary = "按申报查询公示", description = "按申报ID查询公示记录")
    @GetMapping(value = "/queryByApplicationId")
    public Result<List<MrpApprovalPublicity>> queryByApplicationId(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpApprovalPublicity> list = publicityService.list(
                new QueryWrapper<MrpApprovalPublicity>().eq("application_id", applicationId).orderByDesc("create_time"));
        return Result.ok(list);
    }
}

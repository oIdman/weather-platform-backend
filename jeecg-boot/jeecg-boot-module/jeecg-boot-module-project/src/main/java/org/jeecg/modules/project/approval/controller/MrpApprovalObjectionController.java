package org.jeecg.modules.project.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;
import org.jeecg.modules.project.approval.service.IMrpApprovalObjectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 立项异议管理
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "立项异议")
@RestController
@RequestMapping("/project/approval/objection")
@Slf4j
public class MrpApprovalObjectionController {

    @Autowired
    private IMrpApprovalObjectionService objectionService;

    @Operation(summary = "登记异议")
    @PostMapping(value = "/submit")
    public Result<MrpApprovalObjection> submit(@RequestBody MrpApprovalObjection objection) {
        MrpApprovalObjection saved = objectionService.submit(objection);
        return Result.OK("异议提交成功！", saved);
    }

    @Operation(summary = "按公示查询异议", description = "按公示ID查询异议列表")
    @GetMapping(value = "/queryByPublicityId")
    public Result<List<MrpApprovalObjection>> queryByPublicityId(@RequestParam(name = "publicityId", required = true) String publicityId) {
        List<MrpApprovalObjection> list = objectionService.list(
                new QueryWrapper<MrpApprovalObjection>().eq("publicity_id", publicityId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "异议通过id查询", description = "异议通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpApprovalObjection> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpApprovalObjection objection = objectionService.getById(id);
        if (objection == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(objection);
    }
}

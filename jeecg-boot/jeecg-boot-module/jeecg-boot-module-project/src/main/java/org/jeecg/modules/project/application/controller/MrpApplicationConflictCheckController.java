package org.jeecg.modules.project.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.project.application.entity.MrpApplicationConflictCheck;
import org.jeecg.modules.project.application.service.IMrpApplicationConflictCheckService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 申报冲突预检记录查询（记录由系统预检生成，无增删改入口）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "申报冲突预检记录")
@RestController
@RequestMapping("/project/application/conflictCheck")
@Slf4j
public class MrpApplicationConflictCheckController extends JeecgController<MrpApplicationConflictCheck, IMrpApplicationConflictCheckService> {

    @Operation(summary = "按申报查询预检记录", description = "按申报ID查询冲突预检记录")
    @GetMapping(value = "/queryByApplicationId")
    public Result<List<MrpApplicationConflictCheck>> queryByApplicationId(@RequestParam(name = "applicationId", required = true) String applicationId) {
        List<MrpApplicationConflictCheck> list = service.list(
                new QueryWrapper<MrpApplicationConflictCheck>().eq("application_id", applicationId).orderByDesc("check_time"));
        return Result.ok(list);
    }
}

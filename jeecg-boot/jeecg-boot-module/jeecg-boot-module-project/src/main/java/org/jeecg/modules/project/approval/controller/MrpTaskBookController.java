package org.jeecg.modules.project.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.approval.entity.MrpTaskBook;
import org.jeecg.modules.project.approval.service.IMrpTaskBookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 任务书管理（人工起草 / 审批；AI 子项占位）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Tag(name = "项目任务书")
@RestController
@RequestMapping("/project/taskBook")
@Slf4j
public class MrpTaskBookController extends JeecgController<MrpTaskBook, IMrpTaskBookService> {

    @Operation(summary = "任务书分页列表查询", description = "可按项目/状态过滤")
    @GetMapping(value = "/list")
    public Result<IPage<MrpTaskBook>> queryPageList(MrpTaskBook mrpTaskBook,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        QueryWrapper<MrpTaskBook> queryWrapper = QueryGenerator.initQueryWrapper(mrpTaskBook, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpTaskBook> page = new Page<>(pageNo, pageSize);
        IPage<MrpTaskBook> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "任务书添加", description = "人工起草，默认草稿状态")
    @PostMapping(value = "/add")
    public Result<MrpTaskBook> add(@RequestBody MrpTaskBook mrpTaskBook) {
        service.save(mrpTaskBook);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "任务书编辑", description = "仅草稿可编辑")
    @PutMapping(value = "/edit")
    public Result<MrpTaskBook> edit(@RequestBody MrpTaskBook mrpTaskBook) {
        MrpTaskBook byId = service.getById(mrpTaskBook.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        if (!Integer.valueOf(0).equals(byId.getStatus())) {
            return Result.error("仅草稿状态可编辑");
        }
        service.updateById(mrpTaskBook);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "任务书通过id查询", description = "任务书通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpTaskBook> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpTaskBook mrpTaskBook = service.getById(id);
        if (mrpTaskBook == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpTaskBook);
    }

    @Operation(summary = "提交审批", description = "草稿 → 待审批")
    @PostMapping(value = "/submit")
    public Result<MrpTaskBook> submit(@RequestParam(name = "id", required = true) String id) {
        service.submit(id);
        return Result.ok("提交成功！");
    }

    @Operation(summary = "任务书审批", description = "通过/退回")
    @PostMapping(value = "/approve")
    public Result<MrpTaskBook> approve(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        Boolean pass = (Boolean) body.get("pass");
        String approveOpinion = (String) body.get("approveOpinion");
        if (id == null || pass == null) {
            return Result.error("参数不完整！");
        }
        service.approveTaskBook(id, pass, approveOpinion);
        return Result.ok("审批完成！");
    }

    @Operation(summary = "任务书智能起草（AI 占位）", description = "智能生成/风险预警/一致性审核为二期 AI 能力，当前为占位接口")
    @PostMapping(value = "/aiDraft")
    public Result<Map<String, String>> aiDraft(@RequestParam(name = "projectId", required = true) String projectId) {
        Map<String, String> data = new HashMap<>(2);
        data.put("status", "placeholder");
        data.put("message", "任务书智能生成、风险预警、预算编制辅助、内容一致性审核为二期 AI 能力，当前为占位接口");
        return Result.ok(data);
    }
}

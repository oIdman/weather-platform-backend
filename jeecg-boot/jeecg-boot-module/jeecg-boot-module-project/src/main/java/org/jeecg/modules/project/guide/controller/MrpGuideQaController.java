package org.jeecg.modules.project.guide.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.guide.entity.MrpGuideQa;
import org.jeecg.modules.project.guide.service.IMrpGuideQaService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 指南解读答疑管理
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Tag(name = "指南解读答疑")
@RestController
@RequestMapping("/project/guide/qa")
@Slf4j
public class MrpGuideQaController extends JeecgController<MrpGuideQa, IMrpGuideQaService> {

    @Operation(summary = "答疑分页列表查询", description = "答疑分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpGuideQa>> queryPageList(MrpGuideQa mrpGuideQa,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<MrpGuideQa> queryWrapper = QueryGenerator.initQueryWrapper(mrpGuideQa, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<MrpGuideQa> page = new Page<>(pageNo, pageSize);
        IPage<MrpGuideQa> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "按指南查询答疑", description = "按指南ID查询答疑列表")
    @GetMapping(value = "/queryByGuideId")
    public Result<List<MrpGuideQa>> queryByGuideId(@RequestParam(name = "guideId", required = true) String guideId) {
        List<MrpGuideQa> list = service.list(
                new QueryWrapper<MrpGuideQa>().eq("guide_id", guideId).orderByDesc("create_time"));
        return Result.ok(list);
    }

    @Operation(summary = "答疑添加", description = "答疑添加（提问）")
    @PostMapping(value = "/add")
    public Result<MrpGuideQa> add(@RequestBody MrpGuideQa mrpGuideQa) {
        service.save(mrpGuideQa);
        return Result.ok("提问成功！");
    }

    @Operation(summary = "答疑编辑", description = "答疑编辑")
    @PutMapping(value = "/edit")
    public Result<MrpGuideQa> edit(@RequestBody MrpGuideQa mrpGuideQa) {
        MrpGuideQa byId = service.getById(mrpGuideQa.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpGuideQa);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "解答问题", description = "待解答 → 已解答")
    @PostMapping(value = "/answer")
    public Result<MrpGuideQa> answer(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String answer = body.get("answer");
        if (id == null || answer == null) {
            return Result.error("参数不完整！");
        }
        service.answer(id, answer);
        return Result.ok("解答成功！");
    }

    @Operation(summary = "答疑通过id删除", description = "答疑通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpGuideQa> delete(@RequestParam(name = "id", required = true) String id) {
        MrpGuideQa byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "答疑批量删除", description = "答疑批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpGuideQa> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "答疑通过id查询", description = "答疑通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpGuideQa> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpGuideQa mrpGuideQa = service.getById(id);
        if (mrpGuideQa == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpGuideQa);
    }
}

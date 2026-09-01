package org.jeecg.modules.project.researcher.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.project.researcher.entity.MrpResearcher;
import org.jeecg.modules.project.researcher.entity.MrpResearcherAchievement;
import org.jeecg.modules.project.researcher.service.IMrpResearcherAchievementService;
import org.jeecg.modules.project.researcher.service.IMrpResearcherService;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @Description: 科研人员库（最小化脚手架）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Tag(name = "科研人员库")
@RestController
@RequestMapping("/project/researcher")
@Slf4j
public class MrpResearcherController extends JeecgController<MrpResearcher, IMrpResearcherService> {

    @Autowired
    private IMrpResearcherAchievementService mrpResearcherAchievementService;

    @Operation(summary = "科研人员分页列表查询", description = "科研人员分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<MrpResearcher>> queryPageList(MrpResearcher mrpResearcher,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        QueryWrapper<MrpResearcher> queryWrapper = QueryGenerator.initQueryWrapper(mrpResearcher, req.getParameterMap());
        Page<MrpResearcher> page = new Page<>(pageNo, pageSize);
        IPage<MrpResearcher> pageList = service.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    @Operation(summary = "科研人员添加", description = "科研人员添加")
    @PostMapping(value = "/add")
    public Result<MrpResearcher> add(@RequestBody MrpResearcher mrpResearcher) {
        service.save(mrpResearcher);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "科研人员编辑", description = "科研人员编辑")
    @PutMapping(value = "/edit")
    public Result<MrpResearcher> edit(@RequestBody MrpResearcher mrpResearcher) {
        MrpResearcher byId = service.getById(mrpResearcher.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.updateById(mrpResearcher);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "科研人员通过id删除", description = "科研人员通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpResearcher> delete(@RequestParam(name = "id", required = true) String id) {
        MrpResearcher byId = service.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        service.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "科研人员批量删除", description = "科研人员批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpResearcher> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        service.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "科研人员通过id查询", description = "科研人员通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpResearcher> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpResearcher mrpResearcher = service.getById(id);
        if (mrpResearcher == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(mrpResearcher);
    }

    @Operation(summary = "查询科研人员关联成果", description = "按科研人员ID查询既往成果（申报自动带入）")
    @GetMapping(value = "/queryAchievements")
    public Result<List<MrpResearcherAchievement>> queryAchievements(@RequestParam(name = "researcherId", required = true) String researcherId) {
        List<MrpResearcherAchievement> list = mrpResearcherAchievementService.list(
                new QueryWrapper<MrpResearcherAchievement>().eq("researcher_id", researcherId));
        return Result.ok(list);
    }
}

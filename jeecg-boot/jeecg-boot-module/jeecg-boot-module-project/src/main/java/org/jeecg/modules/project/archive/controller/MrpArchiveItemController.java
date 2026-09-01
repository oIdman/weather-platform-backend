package org.jeecg.modules.project.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.project.archive.entity.MrpArchiveItem;
import org.jeecg.modules.project.archive.service.IMrpArchiveItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 归档材料明细管理
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Tag(name = "归档材料明细")
@RestController
@RequestMapping("/project/archive/item")
@Slf4j
public class MrpArchiveItemController {

    @Autowired
    private IMrpArchiveItemService itemService;

    @Operation(summary = "材料清单查询", description = "可按归档/类型过滤")
    @GetMapping(value = "/list")
    public Result<List<MrpArchiveItem>> list(@RequestParam(name = "archiveId", required = false) String archiveId,
                                             @RequestParam(name = "itemType", required = false) String itemType) {
        QueryWrapper<MrpArchiveItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(archiveId != null, "archive_id", archiveId)
                .eq(itemType != null, "item_type", itemType)
                .orderByAsc("sort_order");
        return Result.ok(itemService.list(queryWrapper));
    }

    @Operation(summary = "按归档查询材料", description = "按归档ID查询材料明细")
    @GetMapping(value = "/queryByArchiveId")
    public Result<List<MrpArchiveItem>> queryByArchiveId(@RequestParam(name = "archiveId", required = true) String archiveId) {
        List<MrpArchiveItem> list = itemService.list(
                new QueryWrapper<MrpArchiveItem>().eq("archive_id", archiveId).orderByAsc("sort_order"));
        return Result.ok(list);
    }

    @Operation(summary = "材料添加", description = "补充归档材料（可关联附件 fileId）")
    @PostMapping(value = "/add")
    public Result<MrpArchiveItem> add(@RequestBody MrpArchiveItem item) {
        itemService.save(item);
        return Result.ok("添加成功！");
    }

    @Operation(summary = "材料编辑", description = "材料编辑")
    @PutMapping(value = "/edit")
    public Result<MrpArchiveItem> edit(@RequestBody MrpArchiveItem item) {
        MrpArchiveItem byId = itemService.getById(item.getId());
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        itemService.updateById(item);
        return Result.ok("修改成功！");
    }

    @Operation(summary = "材料通过id删除", description = "材料通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<MrpArchiveItem> delete(@RequestParam(name = "id", required = true) String id) {
        MrpArchiveItem byId = itemService.getById(id);
        if (byId == null) {
            return Result.error("未找到对应实体");
        }
        itemService.removeById(id);
        return Result.ok("删除成功！");
    }

    @Operation(summary = "材料批量删除", description = "材料批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<MrpArchiveItem> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return Result.error("参数不识别！");
        }
        itemService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("删除成功！");
    }

    @Operation(summary = "材料通过id查询", description = "材料通过id查询")
    @GetMapping(value = "/queryById")
    public Result<MrpArchiveItem> queryById(@RequestParam(name = "id", required = true) String id) {
        MrpArchiveItem item = itemService.getById(id);
        if (item == null) {
            return Result.error("未找到对应实体");
        }
        return Result.ok(item);
    }
}

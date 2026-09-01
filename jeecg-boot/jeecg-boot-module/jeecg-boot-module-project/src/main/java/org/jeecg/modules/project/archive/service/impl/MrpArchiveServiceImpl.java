package org.jeecg.modules.project.archive.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceApplication;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceApplicationService;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.entity.MrpTaskBook;
import org.jeecg.modules.project.approval.service.IMrpProjectService;
import org.jeecg.modules.project.approval.service.IMrpTaskBookService;
import org.jeecg.modules.project.archive.entity.MrpArchive;
import org.jeecg.modules.project.archive.entity.MrpArchiveItem;
import org.jeecg.modules.project.archive.mapper.MrpArchiveMapper;
import org.jeecg.modules.project.archive.service.IMrpArchiveItemService;
import org.jeecg.modules.project.archive.service.IMrpArchiveService;
import org.jeecg.modules.project.common.enums.ProjectStatusEnum;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReport;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReportService;
import org.jeecg.modules.project.execution.progress.entity.MrpProgressReport;
import org.jeecg.modules.project.execution.progress.service.IMrpProgressReportService;
import org.jeecg.modules.project.integration.IntegrationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 项目归档 Service 实现（准备自动生成全流程材料清单，完成推送 Mock 档案系统）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Service
public class MrpArchiveServiceImpl extends ServiceImpl<MrpArchiveMapper, MrpArchive> implements IMrpArchiveService {

    @Autowired
    private IMrpProjectService projectService;

    @Autowired
    private IMrpApplicationService applicationService;

    @Autowired
    private IMrpTaskBookService taskBookService;

    @Autowired
    private IMrpMidcheckReportService midcheckReportService;

    @Autowired
    private IMrpProgressReportService progressReportService;

    @Autowired
    private IMrpAcceptanceApplicationService acceptanceApplicationService;

    @Autowired
    private IMrpArchiveItemService archiveItemService;

    @Autowired
    private IntegrationFacade integrationFacade;

    @Override
    public MrpArchive prepare(String projectId) {
        MrpProject project = projectService.getById(projectId);
        if (project == null) {
            throw new ProjectBizException("项目不存在");
        }
        if (!ProjectStatusEnum.ACCEPTED.getCode().equals(project.getStatus())) {
            throw new ProjectBizException("仅已验收项目可归档");
        }
        long exists = count(new QueryWrapper<MrpArchive>().eq("project_id", projectId).eq("status", 0));
        if (exists > 0) {
            throw new ProjectBizException("该项目已有进行中的归档准备");
        }
        MrpArchive archive = new MrpArchive();
        archive.setProjectId(projectId);
        archive.setArchiveNo("GD" + System.currentTimeMillis());
        archive.setArchiveTitle(project.getProjectName() + "项目归档");
        archive.setProjectNo(project.getProjectNo());
        archive.setProjectName(project.getProjectName());
        archive.setCategory(project.getProjectType());
        archive.setStatus(0);
        save(archive);
        generateItems(archive, project);
        return archive;
    }

    @Override
    public void complete(String archiveId) {
        MrpArchive archive = getById(archiveId);
        if (archive == null) {
            throw new ProjectBizException("归档记录不存在");
        }
        if (!Integer.valueOf(0).equals(archive.getStatus())) {
            throw new ProjectBizException("仅准备中状态可完成归档");
        }
        List<MrpArchiveItem> items = archiveItemService.list(
                new QueryWrapper<MrpArchiveItem>().eq("archive_id", archiveId));
        JSONObject pkg = new JSONObject();
        pkg.put("archiveNo", archive.getArchiveNo());
        pkg.put("projectNo", archive.getProjectNo());
        pkg.put("projectName", archive.getProjectName());
        pkg.put("category", archive.getCategory());
        pkg.put("itemCount", items.size());
        pkg.put("archiveTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        String packageJson = pkg.toJSONString();
        archive.setPackageJson(packageJson);
        archive.setStatus(1);
        archive.setArchiveBy("admin");
        archive.setArchiveTime(new Date());
        updateById(archive);
        integrationFacade.pushArchive(packageJson);
        MrpProject project = projectService.getById(archive.getProjectId());
        if (project != null) {
            project.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
            projectService.updateById(project);
        }
    }

    private void generateItems(MrpArchive archive, MrpProject project) {
        List<MrpArchiveItem> items = new ArrayList<>();
        int sort = 1;
        if (oConvertUtils.isNotEmpty(project.getApplicationId())) {
            MrpApplication application = applicationService.getById(project.getApplicationId());
            if (application != null) {
                items.add(buildItem(archive, "application", application.getProjectTitle() + "（申报书）",
                        application.getId(), sort++));
            }
        }
        for (MrpTaskBook taskBook : taskBookService.list(new QueryWrapper<MrpTaskBook>()
                .eq("project_id", project.getId()).eq("status", 2))) {
            items.add(buildItem(archive, "task_book", taskBook.getTitle() + "（任务书）", taskBook.getId(), sort++));
        }
        for (MrpMidcheckReport report : midcheckReportService.list(new QueryWrapper<MrpMidcheckReport>()
                .eq("project_id", project.getId()).ge("status", 1))) {
            items.add(buildItem(archive, "midcheck", report.getReportTitle() + "（中期报告）", report.getId(), sort++));
        }
        for (MrpProgressReport report : progressReportService.list(new QueryWrapper<MrpProgressReport>()
                .eq("project_id", project.getId()).eq("status", 1))) {
            items.add(buildItem(archive, "progress", report.getReportTitle() + "（进展报告）", report.getId(), sort++));
        }
        for (MrpAcceptanceApplication acceptance : acceptanceApplicationService.list(new QueryWrapper<MrpAcceptanceApplication>()
                .eq("project_id", project.getId()).eq("status", 5))) {
            items.add(buildItem(archive, "acceptance", acceptance.getApplicationNo() + "（验收材料）",
                    acceptance.getId(), sort++));
        }
        if (!items.isEmpty()) {
            archiveItemService.saveBatch(items);
        }
    }

    private MrpArchiveItem buildItem(MrpArchive archive, String itemType, String itemName, String sourceId, int sort) {
        MrpArchiveItem item = new MrpArchiveItem();
        item.setArchiveId(archive.getId());
        item.setProjectId(archive.getProjectId());
        item.setItemType(itemType);
        item.setItemName(itemName);
        item.setSourceId(sourceId);
        item.setSortOrder(sort);
        return item;
    }
}

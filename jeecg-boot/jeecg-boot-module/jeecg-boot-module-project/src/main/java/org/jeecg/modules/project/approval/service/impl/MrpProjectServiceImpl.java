package org.jeecg.modules.project.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.enums.ApplicationStatusEnum;
import org.jeecg.modules.project.application.service.IMrpApplicationService;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.mapper.MrpProjectMapper;
import org.jeecg.modules.project.approval.service.IMrpApprovalPublicityService;
import org.jeecg.modules.project.approval.service.IMrpProjectService;
import org.jeecg.modules.project.common.enums.ProjectStatusEnum;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.integration.IntegrationFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: 立项项目 Service 实现（评审通过 → 立项，校验公示结论，通知 Mock）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpProjectServiceImpl extends ServiceImpl<MrpProjectMapper, MrpProject> implements IMrpProjectService {

    @Autowired
    private IMrpApplicationService applicationService;

    @Autowired
    private IMrpApprovalPublicityService publicityService;

    @Autowired
    private IntegrationFacade integrationFacade;

    @Override
    public MrpProject approve(String applicationId) {
        MrpApplication application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ProjectBizException("申报记录不存在");
        }
        if (!ApplicationStatusEnum.APPROVED.getCode().equals(application.getStatus())) {
            throw new ProjectBizException("仅评审通过的申报可立项");
        }
        long exists = count(new QueryWrapper<MrpProject>().eq("application_id", applicationId));
        if (exists > 0) {
            throw new ProjectBizException("该申报已立项");
        }
        List<MrpApprovalPublicity> publicityList = publicityService.list(
                new QueryWrapper<MrpApprovalPublicity>().eq("application_id", applicationId));
        for (MrpApprovalPublicity publicity : publicityList) {
            if (Integer.valueOf(0).equals(publicity.getStatus())) {
                throw new ProjectBizException("公示未结束，不能立项");
            }
            if (Integer.valueOf(2).equals(publicity.getResult())) {
                throw new ProjectBizException("存在成立异议，不能立项");
            }
        }
        MrpProject project = new MrpProject();
        project.setProjectNo("XM" + System.currentTimeMillis());
        project.setApplicationId(applicationId);
        project.setGuideId(application.getGuideId());
        project.setProjectName(application.getProjectTitle());
        project.setProjectType(application.getProjectType());
        project.setResearchField(application.getResearchField());
        project.setLeaderId(application.getApplicantId());
        project.setLeaderName(application.getApplicantName());
        project.setLeaderOrg(application.getApplicantOrg());
        project.setBudget(application.getBudget());
        project.setDurationMonths(application.getDurationMonths());
        project.setSummary(application.getProjectSummary());
        project.setStatus(ProjectStatusEnum.APPROVED.getCode());
        project.setApproveTime(new Date());
        save(project);
        integrationFacade.notifyBySms(application.getApplicantName(), "您的项目《" + application.getProjectTitle() + "》已通过立项审批");
        integrationFacade.pushTodo(application.getApplicantName(), "立项通知", "/project/" + project.getId());
        return project;
    }
}

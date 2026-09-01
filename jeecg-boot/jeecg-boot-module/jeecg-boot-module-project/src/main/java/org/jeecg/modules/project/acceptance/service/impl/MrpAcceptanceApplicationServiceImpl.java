package org.jeecg.modules.project.acceptance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceApplication;
import org.jeecg.modules.project.acceptance.mapper.MrpAcceptanceApplicationMapper;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceApplicationService;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.service.IMrpProjectService;
import org.jeecg.modules.project.common.enums.ProjectStatusEnum;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

/**
 * @Description: 验收申请 Service 实现（提交 → 初审 → Flowable 验收审批 → 通过联动项目状态）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Service
public class MrpAcceptanceApplicationServiceImpl extends ServiceImpl<MrpAcceptanceApplicationMapper, MrpAcceptanceApplication> implements IMrpAcceptanceApplicationService {

    private static final String PROCESS_KEY = "acceptanceReview";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IMrpProjectService projectService;

    @Override
    public void submit(String id) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(0).equals(application.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交");
        }
        application.setApplicationNo("YS" + System.currentTimeMillis());
        application.setStatus(1);
        application.setApplyBy("admin");
        application.setApplyTime(new Date());
        updateById(application);
    }

    @Override
    public void preReview(String id, boolean pass, String opinion) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(1).equals(application.getStatus())) {
            throw new ProjectBizException("仅已提交状态可初审");
        }
        application.setStatus(pass ? 3 : 1);
        application.setPreReviewer("admin");
        application.setPreReviewOpinion(opinion);
        application.setPreReviewTime(new Date());
        updateById(application);
    }

    @Override
    public void startApproval(String id) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(3).equals(application.getStatus())) {
            throw new ProjectBizException("仅待专家评审状态可发起验收审批");
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, id);
        application.setProcessInstanceId(processInstance.getId());
        application.setStatus(4);
        updateById(application);
    }

    @Override
    public void completeApproval(String id, boolean pass, String conclusion) {
        MrpAcceptanceApplication application = getApplicationOrThrow(id);
        if (!Integer.valueOf(4).equals(application.getStatus())) {
            throw new ProjectBizException("仅评审中状态可完成验收审批");
        }
        Task task = taskService.createTaskQuery().processInstanceId(application.getProcessInstanceId()).singleResult();
        if (task == null) {
            throw new ProjectBizException("当前无待处理的验收审批任务");
        }
        taskService.complete(task.getId(), Collections.singletonMap("pass", pass));
        application.setStatus(pass ? 5 : 6);
        application.setConclusion(conclusion);
        application.setFinishTime(new Date());
        updateById(application);
        if (pass) {
            MrpProject project = projectService.getById(application.getProjectId());
            if (project != null) {
                project.setStatus(ProjectStatusEnum.ACCEPTED.getCode());
                projectService.updateById(project);
            }
        }
    }

    private MrpAcceptanceApplication getApplicationOrThrow(String id) {
        MrpAcceptanceApplication application = getById(id);
        if (application == null) {
            throw new ProjectBizException("验收申请不存在");
        }
        return application;
    }
}

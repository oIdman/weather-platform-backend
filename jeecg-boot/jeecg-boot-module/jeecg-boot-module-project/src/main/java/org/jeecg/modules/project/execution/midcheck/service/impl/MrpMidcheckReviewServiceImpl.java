package org.jeecg.modules.project.execution.midcheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.approval.entity.MrpProject;
import org.jeecg.modules.project.approval.service.IMrpProjectService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReport;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReview;
import org.jeecg.modules.project.execution.midcheck.entity.MrpRectificationTask;
import org.jeecg.modules.project.execution.midcheck.mapper.MrpMidcheckReviewMapper;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReportService;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReviewService;
import org.jeecg.modules.project.execution.midcheck.service.IMrpRectificationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Description: 中检评审 Service 实现（Flowable midcheckReview 流程，退回生成整改任务）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpMidcheckReviewServiceImpl extends ServiceImpl<MrpMidcheckReviewMapper, MrpMidcheckReview> implements IMrpMidcheckReviewService {

    private static final String PROCESS_KEY = "midcheckReview";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IMrpMidcheckReportService midcheckReportService;

    @Autowired
    private IMrpRectificationTaskService rectificationTaskService;

    @Autowired
    private IMrpProjectService projectService;

    @Override
    public MrpMidcheckReview startReview(String midcheckReportId) {
        MrpMidcheckReport report = midcheckReportService.getById(midcheckReportId);
        if (report == null) {
            throw new ProjectBizException("中期报告不存在");
        }
        if (!Integer.valueOf(1).equals(report.getStatus())) {
            throw new ProjectBizException("仅已提交状态可发起评审");
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, midcheckReportId);
        MrpMidcheckReview review = new MrpMidcheckReview();
        review.setMidcheckReportId(midcheckReportId);
        review.setProjectId(report.getProjectId());
        review.setProcessInstanceId(processInstance.getId());
        save(review);
        report.setStatus(2);
        midcheckReportService.updateById(report);
        return review;
    }

    @Override
    public void completeReview(String midcheckReportId, boolean pass, String opinion) {
        MrpMidcheckReport report = midcheckReportService.getById(midcheckReportId);
        if (report == null) {
            throw new ProjectBizException("中期报告不存在");
        }
        List<MrpMidcheckReview> reviews = list(new QueryWrapper<MrpMidcheckReview>()
                .eq("midcheck_report_id", midcheckReportId).orderByDesc("create_time"));
        if (reviews.isEmpty()) {
            throw new ProjectBizException("请先发起中检评审");
        }
        MrpMidcheckReview review = reviews.get(0);
        Task task = taskService.createTaskQuery().processInstanceId(review.getProcessInstanceId()).singleResult();
        if (task == null) {
            throw new ProjectBizException("当前无待处理的中检评审任务");
        }
        taskService.complete(task.getId(), Collections.singletonMap("pass", pass));
        review.setReviewer("admin");
        review.setReviewResult(pass ? 1 : 0);
        review.setReviewOpinion(opinion);
        review.setReviewTime(new Date());
        updateById(review);
        report.setStatus(pass ? 3 : 4);
        midcheckReportService.updateById(report);
        if (!pass) {
            MrpProject project = projectService.getById(report.getProjectId());
            MrpRectificationTask taskEntity = new MrpRectificationTask();
            taskEntity.setProjectId(report.getProjectId());
            taskEntity.setMidcheckReviewId(review.getId());
            taskEntity.setTaskTitle("中期检查整改：" + report.getReportTitle());
            taskEntity.setTaskDesc(oConvertUtils.isEmpty(opinion) ? "按中检评审意见整改后重新提交" : opinion);
            taskEntity.setAssignee(project == null ? "项目负责人" : project.getLeaderName());
            taskEntity.setDeadline(new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
            taskEntity.setStatus(0);
            rectificationTaskService.save(taskEntity);
        }
    }
}

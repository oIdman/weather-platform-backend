package org.jeecg.modules.project.approval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;
import org.jeecg.modules.project.approval.entity.MrpApprovalPublicity;
import org.jeecg.modules.project.approval.mapper.MrpApprovalObjectionMapper;
import org.jeecg.modules.project.approval.service.IMrpApprovalObjectionService;
import org.jeecg.modules.project.approval.service.IMrpApprovalPublicityService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

/**
 * @Description: 立项异议 Service 实现（提交启动 Flowable 核查流程，核查完成联动公示结论）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpApprovalObjectionServiceImpl extends ServiceImpl<MrpApprovalObjectionMapper, MrpApprovalObjection> implements IMrpApprovalObjectionService {

    private static final String PROCESS_KEY = "objectionReview";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IMrpApprovalPublicityService publicityService;

    @Override
    public MrpApprovalObjection submit(MrpApprovalObjection objection) {
        if (objection.getPublicityId() == null || objection.getObjectionContent() == null) {
            throw new ProjectBizException("公示ID与异议内容不能为空");
        }
        MrpApprovalPublicity publicity = publicityService.getById(objection.getPublicityId());
        if (publicity == null) {
            throw new ProjectBizException("公示记录不存在");
        }
        if (!Integer.valueOf(0).equals(publicity.getStatus())) {
            throw new ProjectBizException("公示已结束，不再受理异议");
        }
        objection.setObjectionNo("YY" + System.currentTimeMillis());
        objection.setApplicationId(publicity.getApplicationId());
        objection.setStatus(0);
        save(objection);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, objection.getId());
        objection.setProcessInstanceId(processInstance.getId());
        objection.setStatus(1);
        updateById(objection);
        publicityService.markObjectionProcessing(publicity.getId());
        return objection;
    }

    @Override
    public void checkComplete(String id, boolean pass, String checkResult) {
        MrpApprovalObjection objection = getObjectionOrThrow(id);
        if (!Integer.valueOf(1).equals(objection.getStatus())) {
            throw new ProjectBizException("仅核查中状态可完成核查");
        }
        Task task = taskService.createTaskQuery().processInstanceId(objection.getProcessInstanceId()).singleResult();
        if (task != null) {
            taskService.complete(task.getId(), Collections.singletonMap("pass", pass));
        }
        objection.setStatus(pass ? 2 : 3);
        objection.setCheckResult(checkResult);
        objection.setChecker("admin");
        objection.setCheckTime(new Date());
        updateById(objection);
        if (pass) {
            publicityService.setResult(objection.getPublicityId(), 2);
        } else {
            long established = count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MrpApprovalObjection>()
                    .eq("publicity_id", objection.getPublicityId()).eq("status", 2));
            publicityService.setResult(objection.getPublicityId(), established > 0 ? 2 : 0);
        }
    }

    private MrpApprovalObjection getObjectionOrThrow(String id) {
        MrpApprovalObjection objection = getById(id);
        if (objection == null) {
            throw new ProjectBizException("异议记录不存在");
        }
        return objection;
    }
}

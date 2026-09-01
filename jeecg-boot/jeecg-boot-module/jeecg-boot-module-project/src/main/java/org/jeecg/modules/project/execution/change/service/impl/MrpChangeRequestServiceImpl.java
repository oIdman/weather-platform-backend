package org.jeecg.modules.project.execution.change.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.change.entity.MrpChangeRequest;
import org.jeecg.modules.project.execution.change.mapper.MrpChangeRequestMapper;
import org.jeecg.modules.project.execution.change.service.IMrpChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 项目变更 Service 实现（影响评估规则 + Flowable changeApproval 多级审批）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpChangeRequestServiceImpl extends ServiceImpl<MrpChangeRequestMapper, MrpChangeRequest> implements IMrpChangeRequestService {

    private static final String PROCESS_KEY = "changeApproval";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public MrpChangeRequest submit(String id) {
        MrpChangeRequest change = getChangeOrThrow(id);
        if (!Integer.valueOf(0).equals(change.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交审批");
        }
        String impactLevel = assessImpact(change);
        change.setImpactLevel(impactLevel);
        change.setStatus(1);
        change.setApplyBy("admin");
        change.setApplyTime(new Date());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY, id, Collections.singletonMap("impactLevel", impactLevel));
        change.setProcessInstanceId(processInstance.getId());
        updateById(change);
        return change;
    }

    @Override
    public void completeApproval(String id, boolean pass, String opinion) {
        MrpChangeRequest change = getChangeOrThrow(id);
        if (!Integer.valueOf(1).equals(change.getStatus())) {
            throw new ProjectBizException("仅待审批状态可审批");
        }
        Task task = taskService.createTaskQuery().processInstanceId(change.getProcessInstanceId()).singleResult();
        if (task == null) {
            throw new ProjectBizException("当前无待处理的审批任务");
        }
        taskService.complete(task.getId(), Collections.singletonMap("pass", pass));
        long remaining = taskService.createTaskQuery().processInstanceId(change.getProcessInstanceId()).count();
        if (remaining > 0) {
            updateById(change);
            return;
        }
        change.setStatus(pass ? 2 : 3);
        change.setApprover("admin");
        change.setApproveTime(new Date());
        change.setApproveOpinion(opinion);
        updateById(change);
    }

    @Override
    public Map<String, Object> approvalStatus(String id) {
        MrpChangeRequest change = getChangeOrThrow(id);
        Map<String, Object> data = new HashMap<>(4);
        data.put("status", change.getStatus());
        data.put("impactLevel", change.getImpactLevel());
        data.put("processInstanceId", change.getProcessInstanceId());
        if (oConvertUtils.isNotEmpty(change.getProcessInstanceId())) {
            Task task = taskService.createTaskQuery().processInstanceId(change.getProcessInstanceId()).singleResult();
            data.put("currentTask", task == null ? null : task.getName());
        } else {
            data.put("currentTask", null);
        }
        return data;
    }

    /**
     * 影响评估规则（非 AI）：预算变动非空且非 0，或变更类型涉及预算/关键人员/研究方向 → 高风险
     */
    private String assessImpact(MrpChangeRequest change) {
        BigDecimal budgetChange = change.getBudgetChange();
        boolean budgetChanged = budgetChange != null && budgetChange.compareTo(BigDecimal.ZERO) != 0;
        boolean sensitiveType = "budget_change".equals(change.getChangeType())
                || "key_person_change".equals(change.getChangeType())
                || "direction_change".equals(change.getChangeType());
        return budgetChanged || sensitiveType ? "high" : "low";
    }

    private MrpChangeRequest getChangeOrThrow(String id) {
        MrpChangeRequest change = getById(id);
        if (change == null) {
            throw new ProjectBizException("变更申请不存在");
        }
        return change;
    }
}

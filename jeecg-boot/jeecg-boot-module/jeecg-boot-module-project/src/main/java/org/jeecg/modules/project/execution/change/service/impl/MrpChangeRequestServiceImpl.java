package org.jeecg.modules.project.execution.change.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.change.entity.MrpChangeRequest;
import org.jeecg.modules.project.execution.change.mapper.MrpChangeRequestMapper;
import org.jeecg.modules.project.execution.change.service.IMrpChangeRequestService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 项目变更 Service 实现（影响评估规则）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpChangeRequestServiceImpl extends ServiceImpl<MrpChangeRequestMapper, MrpChangeRequest> implements IMrpChangeRequestService {

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
        updateById(change);
        return change;
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

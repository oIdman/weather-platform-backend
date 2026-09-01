package org.jeecg.modules.project.execution.change.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.change.entity.MrpChangeRequest;

import java.util.Map;

/**
 * @Description: 项目变更 Service（影响评估规则 + Flowable 多级审批）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpChangeRequestService extends IService<MrpChangeRequest> {

    /** 提交审批：草稿 → 待审批（规则评估影响等级并启动 Flowable 流程） */
    MrpChangeRequest submit(String id);

    /** 完成当前审批节点（高风险自动进入二级审批） */
    void completeApproval(String id, boolean pass, String opinion);

    /** 审批状态（当前节点/剩余节点数） */
    Map<String, Object> approvalStatus(String id);
}

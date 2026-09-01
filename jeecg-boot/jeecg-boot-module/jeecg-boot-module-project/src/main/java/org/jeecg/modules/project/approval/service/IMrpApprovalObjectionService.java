package org.jeecg.modules.project.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.approval.entity.MrpApprovalObjection;

/**
 * @Description: 立项异议 Service（异议核查走 Flowable 流程）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpApprovalObjectionService extends IService<MrpApprovalObjection> {

    /** 提交异议：启动 Flowable 异议核查流程 */
    MrpApprovalObjection submit(MrpApprovalObjection objection);

    /** 完成核查：结束流程并联动公示结论 */
    void checkComplete(String id, boolean pass, String checkResult);
}

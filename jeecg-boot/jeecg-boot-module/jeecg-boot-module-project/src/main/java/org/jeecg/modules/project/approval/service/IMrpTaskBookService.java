package org.jeecg.modules.project.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.approval.entity.MrpTaskBook;

/**
 * @Description: 任务书 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpTaskBookService extends IService<MrpTaskBook> {

    /** 提交审批：草稿 → 待审批 */
    void submit(String id);

    /** 审批：通过/退回 */
    void approveTaskBook(String id, boolean pass, String approveOpinion);
}

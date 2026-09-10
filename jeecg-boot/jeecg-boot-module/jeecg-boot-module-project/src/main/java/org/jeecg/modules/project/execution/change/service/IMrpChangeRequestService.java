package org.jeecg.modules.project.execution.change.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.change.entity.MrpChangeRequest;

/**
 * @Description: 项目变更 Service（影响评估规则）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpChangeRequestService extends IService<MrpChangeRequest> {

    /** 提交：草稿 → 待处理（规则评估影响等级） */
    MrpChangeRequest submit(String id);
}

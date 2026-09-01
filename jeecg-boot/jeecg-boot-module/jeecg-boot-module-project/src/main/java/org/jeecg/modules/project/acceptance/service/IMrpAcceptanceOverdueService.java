package org.jeecg.modules.project.acceptance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceOverdue;

/**
 * @Description: 验收延期处理 Service
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
public interface IMrpAcceptanceOverdueService extends IService<MrpAcceptanceOverdue> {

    /** 处理：已通报 / 已限制申报 / 已整改销号（状态2会进入申报受限名单） */
    void handle(String id, Integer targetStatus, String handleResult);
}

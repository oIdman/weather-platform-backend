package org.jeecg.modules.project.execution.midcheck.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.midcheck.entity.MrpRectificationTask;

/**
 * @Description: 整改任务 Service（看板）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpRectificationTaskService extends IService<MrpRectificationTask> {

    /** 开始处理：待处理 → 处理中 */
    void start(String id);

    /** 完成整改：处理中 → 已完成 */
    void complete(String id, String handleContent);

    /** 关闭任务：任意状态 → 已关闭 */
    void close(String id);
}

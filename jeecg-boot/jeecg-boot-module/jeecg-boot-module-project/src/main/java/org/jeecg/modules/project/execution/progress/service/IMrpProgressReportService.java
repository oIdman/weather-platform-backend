package org.jeecg.modules.project.execution.progress.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.progress.entity.MrpProgressReport;

/**
 * @Description: 进展报告 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpProgressReportService extends IService<MrpProgressReport> {

    /** 提交：草稿 → 已提交 */
    void submit(String id);
}

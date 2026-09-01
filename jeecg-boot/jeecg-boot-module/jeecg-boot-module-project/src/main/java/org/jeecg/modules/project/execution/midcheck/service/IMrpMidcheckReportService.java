package org.jeecg.modules.project.execution.midcheck.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReport;

/**
 * @Description: 中期报告 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpMidcheckReportService extends IService<MrpMidcheckReport> {

    /** 提交：草稿 → 已提交（自动内容查重 vs 任务书，非 AI 文本相似度） */
    void submit(String id);
}

package org.jeecg.modules.project.execution.progress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.progress.entity.MrpProgressReport;
import org.jeecg.modules.project.execution.progress.mapper.MrpProgressReportMapper;
import org.jeecg.modules.project.execution.progress.service.IMrpProgressReportService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 进展报告 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpProgressReportServiceImpl extends ServiceImpl<MrpProgressReportMapper, MrpProgressReport> implements IMrpProgressReportService {

    @Override
    public void submit(String id) {
        MrpProgressReport report = getById(id);
        if (report == null) {
            throw new ProjectBizException("进展报告不存在");
        }
        if (!Integer.valueOf(0).equals(report.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交");
        }
        if (report.getProgressPercent() == null || report.getProgressPercent() < 0 || report.getProgressPercent() > 100) {
            throw new ProjectBizException("完成度需在 0-100 之间");
        }
        report.setStatus(1);
        report.setSubmitTime(new Date());
        report.setSubmitBy("admin");
        updateById(report);
    }
}

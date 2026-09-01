package org.jeecg.modules.project.execution.midcheck.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.approval.entity.MrpTaskBook;
import org.jeecg.modules.project.approval.service.IMrpTaskBookService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.midcheck.entity.MrpMidcheckReport;
import org.jeecg.modules.project.execution.midcheck.mapper.MrpMidcheckReportMapper;
import org.jeecg.modules.project.execution.midcheck.service.IMrpMidcheckReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 中期报告 Service 实现（提交时 vs 任务书内容查重，非 AI 文本相似度）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpMidcheckReportServiceImpl extends ServiceImpl<MrpMidcheckReportMapper, MrpMidcheckReport> implements IMrpMidcheckReportService {

    @Autowired
    private IMrpTaskBookService taskBookService;

    @Override
    public void submit(String id) {
        MrpMidcheckReport report = getById(id);
        if (report == null) {
            throw new ProjectBizException("中期报告不存在");
        }
        if (!Integer.valueOf(0).equals(report.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交");
        }
        String taskBookContent = findLatestPassedTaskBookContent(report.getProjectId());
        if (oConvertUtils.isNotEmpty(taskBookContent) && oConvertUtils.isNotEmpty(report.getContent())) {
            int rate = Math.round((float) (jaccardSimilarity(report.getContent(), taskBookContent) * 100));
            report.setSimilarRate(new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP));
            report.setSimilarDetail("与任务书内容相似度 " + rate + "%，大段落雷同需人工复核");
        } else {
            report.setSimilarRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            report.setSimilarDetail("未匹配到可查重对照（任务书内容为空）");
        }
        report.setStatus(1);
        report.setSubmitTime(new Date());
        report.setSubmitBy("admin");
        updateById(report);
    }

    private String findLatestPassedTaskBookContent(String projectId) {
        List<MrpTaskBook> taskBooks = taskBookService.list(new QueryWrapper<MrpTaskBook>()
                .eq("project_id", projectId).eq("status", 2).orderByDesc("create_time"));
        return taskBooks.isEmpty() ? null : taskBooks.get(0).getContent();
    }

    private double jaccardSimilarity(String a, String b) {
        Set<String> gramsA = ngrams(a, 2);
        Set<String> gramsB = ngrams(b, 2);
        if (gramsA.isEmpty() || gramsB.isEmpty()) {
            return 0;
        }
        Set<String> union = new HashSet<>(gramsA);
        union.addAll(gramsB);
        Set<String> intersection = new HashSet<>(gramsA);
        intersection.retainAll(gramsB);
        return (double) intersection.size() / union.size();
    }

    private Set<String> ngrams(String text, int n) {
        Set<String> result = new HashSet<>();
        if (text == null || text.length() < n) {
            return result;
        }
        for (int i = 0; i <= text.length() - n; i++) {
            result.add(text.substring(i, i + n));
        }
        return result;
    }
}

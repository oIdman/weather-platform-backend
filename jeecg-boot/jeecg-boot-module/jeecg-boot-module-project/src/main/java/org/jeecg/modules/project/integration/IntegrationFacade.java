package org.jeecg.modules.project.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 外部对接统一门面。业务模块统一通过本类访问外部能力，
 * 本期全部走 Mock 实现，二期替换为真实对接无需改动业务代码。
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Component
@RequiredArgsConstructor
public class IntegrationFacade {

    private final SmsService smsService;
    private final OaService oaService;
    private final FinanceService financeService;
    private final ArchiveService archiveService;
    private final AcademicService academicService;

    /** 短信通知 */
    public void notifyBySms(String phone, String content) {
        smsService.sendSms(phone, content);
    }

    /** OA 待办推送 */
    public void pushTodo(String userId, String title, String url) {
        oaService.pushTodo(userId, title, url);
    }

    /** 归档包推送 */
    public void pushArchive(String archivePackageJson) {
        archiveService.pushArchive(archivePackageJson);
    }

    /** 项目经费执行查询 */
    public BigDecimal queryBudgetUsage(String projectId) {
        return financeService.queryBudgetUsage(projectId);
    }

    /** 论文检索 */
    public List<String> searchPapers(String keyword, int limit) {
        return academicService.searchPapers(keyword, limit);
    }
}

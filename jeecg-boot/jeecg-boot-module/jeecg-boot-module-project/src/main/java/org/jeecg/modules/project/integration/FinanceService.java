package org.jeecg.modules.project.integration;

import java.math.BigDecimal;

/**
 * @Description: 财务系统服务（外部对接接口，本期 Mock；经费模块二期接入）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public interface FinanceService {

    /**
     * 查询项目经费已执行金额
     *
     * @param projectId 项目ID
     * @return 已执行金额
     */
    BigDecimal queryBudgetUsage(String projectId);

    /**
     * 判断金额是否超出项目预算
     *
     * @param projectId 项目ID
     * @param amount    待校验金额
     * @return true-超预算
     */
    boolean isBudgetExceeded(String projectId, BigDecimal amount);
}

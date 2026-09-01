package org.jeecg.modules.project.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @Description: 财务服务 Mock 实现（二期对接真实财务系统）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Slf4j
@Service("financeServiceMock")
public class FinanceServiceMock implements FinanceService {

    @Override
    public BigDecimal queryBudgetUsage(String projectId) {
        log.info("[mock-finance] queryBudgetUsage projectId={}", projectId);
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isBudgetExceeded(String projectId, BigDecimal amount) {
        log.info("[mock-finance] isBudgetExceeded projectId={}, amount={}", projectId, amount);
        return false;
    }
}

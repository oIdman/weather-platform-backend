package org.jeecg.modules.project.acceptance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceOverdue;
import org.jeecg.modules.project.acceptance.mapper.MrpAcceptanceOverdueMapper;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceOverdueService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

/**
 * @Description: 验收延期处理 Service 实现（status=2 已限制申报，进入 Phase 2 冲突预检受限名单）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Service
public class MrpAcceptanceOverdueServiceImpl extends ServiceImpl<MrpAcceptanceOverdueMapper, MrpAcceptanceOverdue> implements IMrpAcceptanceOverdueService {

    @Override
    public void handle(String id, Integer targetStatus, String handleResult) {
        MrpAcceptanceOverdue overdue = getById(id);
        if (overdue == null) {
            throw new ProjectBizException("延期处理记录不存在");
        }
        if (targetStatus == null || !Arrays.asList(1, 2, 3).contains(targetStatus) || targetStatus.equals(overdue.getStatus())) {
            throw new ProjectBizException("非法的目标处理状态");
        }
        overdue.setStatus(targetStatus);
        overdue.setHandleResult(handleResult);
        overdue.setHandleTime(new Date());
        updateById(overdue);
    }
}

package org.jeecg.modules.project.execution.midcheck.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.execution.midcheck.entity.MrpRectificationTask;
import org.jeecg.modules.project.execution.midcheck.mapper.MrpRectificationTaskMapper;
import org.jeecg.modules.project.execution.midcheck.service.IMrpRectificationTaskService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 整改任务 Service 实现（看板流转）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpRectificationTaskServiceImpl extends ServiceImpl<MrpRectificationTaskMapper, MrpRectificationTask> implements IMrpRectificationTaskService {

    @Override
    public void start(String id) {
        MrpRectificationTask task = getTaskOrThrow(id);
        if (!Integer.valueOf(0).equals(task.getStatus())) {
            throw new ProjectBizException("仅待处理状态可开始");
        }
        task.setStatus(1);
        updateById(task);
    }

    @Override
    public void complete(String id, String handleContent) {
        MrpRectificationTask task = getTaskOrThrow(id);
        if (Integer.valueOf(2).equals(task.getStatus()) || Integer.valueOf(3).equals(task.getStatus())) {
            throw new ProjectBizException("任务已完成或已关闭");
        }
        task.setStatus(2);
        task.setHandleContent(handleContent);
        task.setHandleTime(new Date());
        updateById(task);
    }

    @Override
    public void close(String id) {
        MrpRectificationTask task = getTaskOrThrow(id);
        task.setStatus(3);
        updateById(task);
    }

    private MrpRectificationTask getTaskOrThrow(String id) {
        MrpRectificationTask task = getById(id);
        if (task == null) {
            throw new ProjectBizException("整改任务不存在");
        }
        return task;
    }
}

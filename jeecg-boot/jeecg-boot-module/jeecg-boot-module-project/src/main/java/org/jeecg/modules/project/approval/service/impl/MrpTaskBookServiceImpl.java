package org.jeecg.modules.project.approval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.approval.entity.MrpTaskBook;
import org.jeecg.modules.project.approval.mapper.MrpTaskBookMapper;
import org.jeecg.modules.project.approval.service.IMrpTaskBookService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 任务书 Service 实现（草稿 → 待审批 → 通过/退回）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Service
public class MrpTaskBookServiceImpl extends ServiceImpl<MrpTaskBookMapper, MrpTaskBook> implements IMrpTaskBookService {

    @Override
    public void submit(String id) {
        MrpTaskBook taskBook = getTaskBookOrThrow(id);
        if (!Integer.valueOf(0).equals(taskBook.getStatus())) {
            throw new ProjectBizException("仅草稿状态可提交审批");
        }
        taskBook.setStatus(1);
        updateById(taskBook);
    }

    @Override
    public void approveTaskBook(String id, boolean pass, String approveOpinion) {
        MrpTaskBook taskBook = getTaskBookOrThrow(id);
        if (!Integer.valueOf(1).equals(taskBook.getStatus())) {
            throw new ProjectBizException("仅待审批状态可审批");
        }
        taskBook.setStatus(pass ? 2 : 3);
        taskBook.setApprover("admin");
        taskBook.setApproveTime(new Date());
        taskBook.setApproveOpinion(approveOpinion);
        updateById(taskBook);
    }

    private MrpTaskBook getTaskBookOrThrow(String id) {
        MrpTaskBook taskBook = getById(id);
        if (taskBook == null) {
            throw new ProjectBizException("任务书不存在");
        }
        return taskBook;
    }
}

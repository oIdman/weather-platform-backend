package org.jeecg.modules.project.acceptance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceMeeting;
import org.jeecg.modules.project.acceptance.mapper.MrpAcceptanceMeetingMapper;
import org.jeecg.modules.project.acceptance.service.IMrpAcceptanceMeetingService;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.springframework.stereotype.Service;

/**
 * @Description: 验收会议 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Service
public class MrpAcceptanceMeetingServiceImpl extends ServiceImpl<MrpAcceptanceMeetingMapper, MrpAcceptanceMeeting> implements IMrpAcceptanceMeetingService {

    @Override
    public void confirm(String id) {
        MrpAcceptanceMeeting meeting = getMeetingOrThrow(id);
        if (!Integer.valueOf(0).equals(meeting.getStatus())) {
            throw new ProjectBizException("仅待组织状态可预约");
        }
        meeting.setStatus(1);
        updateById(meeting);
    }

    @Override
    public void hold(String id) {
        MrpAcceptanceMeeting meeting = getMeetingOrThrow(id);
        if (Integer.valueOf(2).equals(meeting.getStatus()) || Integer.valueOf(3).equals(meeting.getStatus())) {
            throw new ProjectBizException("会议已召开或已取消");
        }
        meeting.setStatus(2);
        updateById(meeting);
    }

    private MrpAcceptanceMeeting getMeetingOrThrow(String id) {
        MrpAcceptanceMeeting meeting = getById(id);
        if (meeting == null) {
            throw new ProjectBizException("验收会议不存在");
        }
        return meeting;
    }
}

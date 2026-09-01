package org.jeecg.modules.project.acceptance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.acceptance.entity.MrpAcceptanceMeeting;

/**
 * @Description: 验收会议 Service
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
public interface IMrpAcceptanceMeetingService extends IService<MrpAcceptanceMeeting> {

    /** 预约：待组织 → 已预约 */
    void confirm(String id);

    /** 召开：待组织/已预约 → 已召开 */
    void hold(String id);
}

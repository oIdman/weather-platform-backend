package org.jeecg.modules.project.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.approval.entity.MrpProject;

/**
 * @Description: 立项项目 Service
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
public interface IMrpProjectService extends IService<MrpProject> {

    /** 立项：由评审通过的申报生成项目主表，并触发通知（Mock） */
    MrpProject approve(String applicationId);
}

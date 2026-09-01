package org.jeecg.modules.project.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.archive.entity.MrpArchive;

/**
 * @Description: 项目归档 Service
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
public interface IMrpArchiveService extends IService<MrpArchive> {

    /** 归档准备：校验项目已验收，创建归档主表并自动生成全流程材料清单 */
    MrpArchive prepare(String projectId);

    /** 完成归档：推送电子档案包（Mock）并联动项目状态为已归档 */
    void complete(String archiveId);
}

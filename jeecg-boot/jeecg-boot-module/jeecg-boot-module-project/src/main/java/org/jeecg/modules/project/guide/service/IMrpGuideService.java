package org.jeecg.modules.project.guide.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.guide.entity.MrpGuide;

/**
 * @Description: 课题指南 Service
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
public interface IMrpGuideService extends IService<MrpGuide> {

    /** 提交发布：草稿 → 待发布 */
    void submit(String id);

    /** 发布：待发布 → 已发布（生成版本快照） */
    void publish(String id);

    /** 归档：已发布 → 已归档 */
    void archive(String id);

    /** 校验是否可编辑/删除（仅草稿、待发布可编辑） */
    void checkEditable(MrpGuide guide);
}

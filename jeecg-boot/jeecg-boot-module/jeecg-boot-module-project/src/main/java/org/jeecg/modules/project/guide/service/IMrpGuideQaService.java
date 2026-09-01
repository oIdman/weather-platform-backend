package org.jeecg.modules.project.guide.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.guide.entity.MrpGuideQa;

/**
 * @Description: 指南答疑 Service
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
public interface IMrpGuideQaService extends IService<MrpGuideQa> {

    /** 解答问题：待解答 → 已解答 */
    void answer(String id, String answer);
}

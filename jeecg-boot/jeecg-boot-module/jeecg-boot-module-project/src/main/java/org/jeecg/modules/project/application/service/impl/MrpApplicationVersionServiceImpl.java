package org.jeecg.modules.project.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.application.entity.MrpApplicationVersion;
import org.jeecg.modules.project.application.mapper.MrpApplicationVersionMapper;
import org.jeecg.modules.project.application.service.IMrpApplicationVersionService;
import org.springframework.stereotype.Service;

/**
 * @Description: 申报版本快照 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Service
public class MrpApplicationVersionServiceImpl extends ServiceImpl<MrpApplicationVersionMapper, MrpApplicationVersion> implements IMrpApplicationVersionService {
}

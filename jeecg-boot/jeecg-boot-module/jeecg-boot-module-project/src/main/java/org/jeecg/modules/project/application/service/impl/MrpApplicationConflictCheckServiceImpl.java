package org.jeecg.modules.project.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.project.application.entity.MrpApplicationConflictCheck;
import org.jeecg.modules.project.application.mapper.MrpApplicationConflictCheckMapper;
import org.jeecg.modules.project.application.service.IMrpApplicationConflictCheckService;
import org.springframework.stereotype.Service;

/**
 * @Description: 申报冲突预检 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Service
public class MrpApplicationConflictCheckServiceImpl extends ServiceImpl<MrpApplicationConflictCheckMapper, MrpApplicationConflictCheck> implements IMrpApplicationConflictCheckService {
}

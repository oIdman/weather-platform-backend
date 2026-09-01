package org.jeecg.modules.project.guide.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.guide.entity.MrpGuideQa;
import org.jeecg.modules.project.guide.mapper.MrpGuideQaMapper;
import org.jeecg.modules.project.guide.service.IMrpGuideQaService;
import org.springframework.stereotype.Service;

/**
 * @Description: 指南答疑 Service 实现
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Service
public class MrpGuideQaServiceImpl extends ServiceImpl<MrpGuideQaMapper, MrpGuideQa> implements IMrpGuideQaService {

    @Override
    public void answer(String id, String answer) {
        MrpGuideQa qa = getById(id);
        if (qa == null) {
            throw new ProjectBizException("答疑记录不存在");
        }
        if (Integer.valueOf(1).equals(qa.getStatus())) {
            throw new ProjectBizException("该问题已解答");
        }
        qa.setAnswer(answer);
        qa.setAnswerer(getLoginUsername());
        qa.setStatus(1);
        updateById(qa);
    }

    private String getLoginUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof LoginUser) {
                return ((LoginUser) principal).getUsername();
            }
        } catch (Exception e) {
            // 未登录场景直接返回 null
        }
        return null;
    }
}

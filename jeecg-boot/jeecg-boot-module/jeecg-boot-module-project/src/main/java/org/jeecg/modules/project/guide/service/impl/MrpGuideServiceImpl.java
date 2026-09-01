package org.jeecg.modules.project.guide.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.project.common.exception.ProjectBizException;
import org.jeecg.modules.project.guide.entity.MrpGuide;
import org.jeecg.modules.project.guide.entity.MrpGuideVersion;
import org.jeecg.modules.project.guide.enums.GuideStatusEnum;
import org.jeecg.modules.project.guide.mapper.MrpGuideMapper;
import org.jeecg.modules.project.guide.service.IMrpGuideService;
import org.jeecg.modules.project.guide.service.IMrpGuideVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 课题指南 Service 实现（状态机：草稿→待发布→已发布→已归档）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Service
public class MrpGuideServiceImpl extends ServiceImpl<MrpGuideMapper, MrpGuide> implements IMrpGuideService {

    @Autowired
    private IMrpGuideVersionService guideVersionService;

    @Override
    public void submit(String id) {
        MrpGuide guide = getGuideOrThrow(id);
        checkStatus(guide, GuideStatusEnum.DRAFT, "仅草稿状态可提交发布");
        guide.setStatus(GuideStatusEnum.PENDING_PUBLISH.getCode());
        updateById(guide);
    }

    @Override
    public void publish(String id) {
        MrpGuide guide = getGuideOrThrow(id);
        checkStatus(guide, GuideStatusEnum.PENDING_PUBLISH, "仅待发布状态可发布");
        long count = guideVersionService.count(new QueryWrapper<MrpGuideVersion>().eq("guide_id", id));
        MrpGuideVersion version = new MrpGuideVersion();
        version.setGuideId(id);
        version.setVersionNo("V" + (count + 1));
        version.setGuideTitle(guide.getGuideTitle());
        version.setGuideYear(guide.getGuideYear());
        version.setStage(guide.getStage());
        version.setContent(guide.getContent());
        version.setChangeNote("发布快照");
        version.setPublishTime(new Date());
        guideVersionService.save(version);
        guide.setStatus(GuideStatusEnum.PUBLISHED.getCode());
        guide.setPublisher(getLoginUsername());
        guide.setPublishTime(new Date());
        updateById(guide);
    }

    @Override
    public void archive(String id) {
        MrpGuide guide = getGuideOrThrow(id);
        checkStatus(guide, GuideStatusEnum.PUBLISHED, "仅已发布状态可归档");
        guide.setStatus(GuideStatusEnum.ARCHIVED.getCode());
        guide.setArchiveTime(new Date());
        updateById(guide);
    }

    @Override
    public void checkEditable(MrpGuide guide) {
        if (guide != null && guide.getStatus() != null
                && !GuideStatusEnum.DRAFT.getCode().equals(guide.getStatus())
                && !GuideStatusEnum.PENDING_PUBLISH.getCode().equals(guide.getStatus())) {
            throw new ProjectBizException("已发布/已归档指南不可编辑或删除");
        }
    }

    private MrpGuide getGuideOrThrow(String id) {
        MrpGuide guide = getById(id);
        if (guide == null) {
            throw new ProjectBizException("指南不存在");
        }
        return guide;
    }

    private void checkStatus(MrpGuide guide, GuideStatusEnum expect, String message) {
        if (!expect.getCode().equals(guide.getStatus())) {
            throw new ProjectBizException(message);
        }
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

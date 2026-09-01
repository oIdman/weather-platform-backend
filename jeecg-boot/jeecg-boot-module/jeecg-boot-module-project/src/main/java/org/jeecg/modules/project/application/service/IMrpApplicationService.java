package org.jeecg.modules.project.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.project.application.entity.MrpApplication;
import org.jeecg.modules.project.application.entity.MrpApplicationConflictCheck;

import java.util.List;

/**
 * @Description: 课题申报 Service
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
public interface IMrpApplicationService extends IService<MrpApplication> {

    /** 从指南预填（指南标题、项目类型） */
    void prefillFromGuide(MrpApplication application);

    /** 从科研人员库自动带入履历（姓名/机构/职称） */
    void prefillApplicant(MrpApplication application);

    /** 提交：草稿 → 已提交（自动冲突预检 + 生成版本快照） */
    void submit(String id);

    /** 撤回：已提交 → 草稿 */
    void withdraw(String id);

    /** 校验是否可编辑/删除（仅草稿） */
    void checkEditable(MrpApplication application);

    /** 运行冲突预检（不落库，由调用方决定保存） */
    List<MrpApplicationConflictCheck> runConflictCheck(MrpApplication application);
}

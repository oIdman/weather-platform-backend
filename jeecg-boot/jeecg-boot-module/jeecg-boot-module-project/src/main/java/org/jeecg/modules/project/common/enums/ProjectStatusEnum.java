package org.jeecg.modules.project.common.enums;

/**
 * @Description: 项目生命周期状态（mrp_project.status）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public enum ProjectStatusEnum {

    PENDING_PUBLICITY(0, "待公示"),
    PUBLICITY(1, "公示中"),
    APPROVED(2, "已立项"),
    IN_PROGRESS(3, "执行中"),
    ACCEPTANCE_PENDING(4, "待验收"),
    ACCEPTED(5, "已验收"),
    ARCHIVED(6, "已归档"),
    TERMINATED(7, "已终止");

    private final Integer code;
    private final String text;

    ProjectStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ProjectStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProjectStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}

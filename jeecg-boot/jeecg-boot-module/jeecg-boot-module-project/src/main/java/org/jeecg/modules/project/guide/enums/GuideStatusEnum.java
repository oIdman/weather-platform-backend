package org.jeecg.modules.project.guide.enums;

/**
 * @Description: 课题指南状态（mrp_guide.status）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
public enum GuideStatusEnum {

    DRAFT(0, "草稿"),
    PENDING_PUBLISH(1, "待发布"),
    PUBLISHED(2, "已发布"),
    ARCHIVED(3, "已归档");

    private final Integer code;
    private final String text;

    GuideStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static GuideStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (GuideStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}

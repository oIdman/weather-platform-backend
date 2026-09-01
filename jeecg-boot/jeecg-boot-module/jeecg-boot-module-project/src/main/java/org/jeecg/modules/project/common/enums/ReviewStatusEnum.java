package org.jeecg.modules.project.common.enums;

/**
 * @Description: 申报审查状态（形式审查 / 专家评审通用）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public enum ReviewStatusEnum {

    PENDING(0, "待审查"),
    REVIEWING(1, "审查中"),
    PASSED(2, "已通过"),
    REJECTED(3, "已退回");

    private final Integer code;
    private final String text;

    ReviewStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ReviewStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReviewStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}

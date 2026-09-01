package org.jeecg.modules.project.application.enums;

/**
 * @Description: 课题申报状态（mrp_application.status）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
public enum ApplicationStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    FORMAL_REVIEW(2, "形式审查中"),
    EXPERT_REVIEW(3, "专家评审中"),
    APPROVED(4, "评审通过"),
    REJECTED(5, "已拒绝"),
    RECTIFYING(6, "整改中");

    private final Integer code;
    private final String text;

    ApplicationStatusEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ApplicationStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApplicationStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}

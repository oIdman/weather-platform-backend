package org.jeecg.modules.project.common.enums;

/**
 * @Description: 验收结论等级
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public enum AcceptanceLevelEnum {

    EXCELLENT("excellent", "优秀"),
    QUALIFIED("qualified", "合格"),
    UNQUALIFIED("unqualified", "不合格");

    private final String code;
    private final String text;

    AcceptanceLevelEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}

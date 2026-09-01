package org.jeecg.modules.project.common.enums;

/**
 * @Description: 公共附件业务类型（mrp_attachment.biz_type）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
public enum AttachmentBizTypeEnum {

    GUIDE("guide", "课题指南"),
    APPLICATION("application", "课题申报"),
    REVIEW("review", "申报审查"),
    PROJECT("project", "立项项目"),
    MIDCHECK("midcheck", "中期检查"),
    ACCEPTANCE("acceptance", "验收"),
    ARCHIVE("archive", "归档");

    private final String code;
    private final String text;

    AttachmentBizTypeEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static boolean contains(String code) {
        if (code == null) {
            return false;
        }
        for (AttachmentBizTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}

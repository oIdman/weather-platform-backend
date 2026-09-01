package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 指南版本快照（发布时生成，支撑历史比对 / 版本差异对比）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide_version")
public class MrpGuideVersion extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 版本号（如 V1） */
    @Schema(description = "版本号（如 V1）")
    private String versionNo;

    /** 指南标题快照 */
    @Schema(description = "指南标题快照")
    private String guideTitle;

    /** 指南年度快照 */
    @Schema(description = "指南年度快照")
    private String guideYear;

    /** 阶段/批次快照 */
    @Schema(description = "阶段/批次快照")
    private String stage;

    /** 指南正文快照 */
    @Schema(description = "指南正文快照")
    private String content;

    /** 变更说明 */
    @Schema(description = "变更说明")
    private String changeNote;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;
}

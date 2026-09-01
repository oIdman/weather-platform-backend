package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 指南模板库（年度 / 阶段模板）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide_template")
public class MrpGuideTemplate extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    @Schema(description = "模板名称")
    private String templateName;

    /** 模板年度 */
    @Schema(description = "模板年度")
    private String templateYear;

    /** 阶段/批次 */
    @Schema(description = "阶段/批次")
    private String stage;

    /** 模板内容 */
    @Schema(description = "模板内容")
    private String templateContent;

    /** 是否默认(0-否,1-是) */
    @Schema(description = "是否默认(0-否,1-是)")
    private Integer isDefault;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

package org.jeecg.modules.project.archive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 归档模板（按项目类别）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_archive_template")
public class MrpArchiveTemplate extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    @Schema(description = "模板名称")
    private String templateName;

    /** 适用项目类别 */
    @Schema(description = "适用项目类别")
    private String category;

    /** 模板内容（材料清单等） */
    @Schema(description = "模板内容（材料清单等）")
    private String templateContent;

    /** 是否默认(0-否,1-是) */
    @Schema(description = "是否默认(0-否,1-是)")
    private Integer isDefault;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

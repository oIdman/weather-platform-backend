package org.jeecg.modules.project.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 申报版本快照（提交时生成，支撑断点续填 / 多版本管理）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_application_version")
public class MrpApplicationVersion extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 版本号（如 V1） */
    @Schema(description = "版本号（如 V1）")
    private String versionNo;

    /** 项目名称快照 */
    @Schema(description = "项目名称快照")
    private String projectTitle;

    /** 研究方案快照 */
    @Schema(description = "研究方案快照")
    private String projectSummary;

    /** 申报内容完整快照(JSON) */
    @Schema(description = "申报内容完整快照(JSON)")
    private String content;

    /** 变更说明 */
    @Schema(description = "变更说明")
    private String changeNote;
}

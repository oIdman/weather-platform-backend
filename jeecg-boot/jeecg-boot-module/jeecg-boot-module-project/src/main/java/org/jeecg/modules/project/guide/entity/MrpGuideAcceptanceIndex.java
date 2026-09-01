package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 验收考核指标项（按课题分类预置）
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide_acceptance_index")
public class MrpGuideAcceptanceIndex extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 课题类目ID（mrp_guide_category） */
    @Schema(description = "课题类目ID（mrp_guide_category）")
    private String categoryId;

    /** 指标项名称 */
    @Schema(description = "指标项名称")
    private String indexName;

    /** 指标说明 */
    @Schema(description = "指标说明")
    private String indexDesc;

    /** 是否必查(0-否,1-是) */
    @Schema(description = "是否必查(0-否,1-是)")
    private Integer isRequired;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

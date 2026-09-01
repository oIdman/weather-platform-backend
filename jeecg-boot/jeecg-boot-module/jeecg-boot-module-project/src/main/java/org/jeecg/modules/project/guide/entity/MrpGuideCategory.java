package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 指南类目
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide_category")
public class MrpGuideCategory extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 父类目ID */
    @Schema(description = "父类目ID")
    private String parentId;

    /** 类目名称 */
    @Schema(description = "类目名称")
    private String categoryName;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

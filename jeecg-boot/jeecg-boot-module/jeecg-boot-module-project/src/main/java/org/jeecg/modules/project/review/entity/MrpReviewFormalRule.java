package org.jeecg.modules.project.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 形式审查规则库（可配置）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_formal_rule")
public class MrpReviewFormalRule extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则名称 */
    @Schema(description = "规则名称")
    private String ruleName;

    /** 规则类型（completeness-完整性,format-格式规范,qualification-资格） */
    @Schema(description = "规则类型（completeness-完整性,format-格式规范,qualification-资格）")
    private String ruleType;

    /** 规则说明/校验描述 */
    @Schema(description = "规则说明/校验描述")
    private String ruleExpression;

    /** 是否启用(0-否,1-是) */
    @Schema(description = "是否启用(0-否,1-是)")
    private Integer isEnabled;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

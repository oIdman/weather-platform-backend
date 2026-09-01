package org.jeecg.modules.project.acceptance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

import java.math.BigDecimal;

/**
 * @Description: 验收打分（任务完成度 / 成果质量 / 汇报答辩）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_acceptance_score")
public class MrpAcceptanceScore extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 验收申请ID */
    @Schema(description = "验收申请ID")
    private String acceptanceApplicationId;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 专家ID（mrp_expert.id） */
    @Schema(description = "专家ID（mrp_expert.id）")
    private String expertId;

    /** 专家姓名快照 */
    @Schema(description = "专家姓名快照")
    private String expertName;

    /** 打分维度（completion/achievement/presentation） */
    @Schema(description = "打分维度（completion/achievement/presentation）")
    private String dimension;

    /** 分值 */
    @Schema(description = "分值")
    private BigDecimal score;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

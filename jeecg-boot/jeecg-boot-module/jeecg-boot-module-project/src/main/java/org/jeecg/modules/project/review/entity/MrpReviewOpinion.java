package org.jeecg.modules.project.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 专家评审意见
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_opinion")
public class MrpReviewOpinion extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 分配ID */
    @Schema(description = "分配ID")
    private String assignmentId;

    /** 专家ID */
    @Schema(description = "专家ID")
    private String expertId;

    /** 专家姓名快照 */
    @Schema(description = "专家姓名快照")
    private String expertName;

    /** 意见类型（overall-总体意见,issue-问题清单,suggestion-建议） */
    @Schema(description = "意见类型（overall-总体意见,issue-问题清单,suggestion-建议）")
    private String opinionType;

    /** 意见内容 */
    @Schema(description = "意见内容")
    private String opinionContent;
}

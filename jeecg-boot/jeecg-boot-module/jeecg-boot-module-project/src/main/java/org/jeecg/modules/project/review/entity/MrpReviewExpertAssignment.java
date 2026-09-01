package org.jeecg.modules.project.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 专家评审分配（含回避规则记录 / 抽取批次）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_expert_assignment")
public class MrpReviewExpertAssignment extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 专家ID（mrp_expert.id） */
    @Schema(description = "专家ID（mrp_expert.id）")
    private String expertId;

    /** 专家姓名快照 */
    @Schema(description = "专家姓名快照")
    private String expertName;

    /** 专家领域快照 */
    @Schema(description = "专家领域快照")
    private String expertField;

    /** 抽取批次 */
    @Schema(description = "抽取批次")
    private Integer assignBatch;

    /** 分配方式（manual-人工,auto_random-自动随机） */
    @Schema(description = "分配方式（manual-人工,auto_random-自动随机）")
    private String assignWay;

    /** 是否已做回避检测(0-否,1-是) */
    @Schema(description = "是否已做回避检测(0-否,1-是)")
    private Integer avoidChecked;

    /** 回避检测结果(1-无回避,0-命中回避) */
    @Schema(description = "回避检测结果(1-无回避,0-命中回避)")
    private Integer avoidResult;

    /** 状态(0-待评审,1-已接受,2-已完成,3-已拒绝) */
    @Schema(description = "状态(0-待评审,1-已接受,2-已完成,3-已拒绝)")
    private Integer status;

    /** 分配时间 */
    @Schema(description = "分配时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignedTime;

    /** 完成时间 */
    @Schema(description = "完成时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;
}

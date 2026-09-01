package org.jeecg.modules.project.review.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 专家打分（多维度，含评审起止时间）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_score")
public class MrpReviewScore extends MrpBaseEntity {

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

    /** 打分维度（innovation/feasibility/research_basis） */
    @Schema(description = "打分维度（innovation/feasibility/research_basis）")
    private String dimension;

    /** 分值 */
    @Schema(description = "分值")
    private BigDecimal score;

    /** 评审开始时间 */
    @Schema(description = "评审开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    /** 评审结束时间 */
    @Schema(description = "评审结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

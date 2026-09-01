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
 * @Description: 形式审查整改-重报闭环记录
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_formal_rectification")
public class MrpReviewFormalRectification extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 整改单号 */
    @Schema(description = "整改单号")
    private String rectificationNo;

    /** 问题清单 */
    @Schema(description = "问题清单")
    private String issueDesc;

    /** 状态(0-待整改,1-已整改重报,2-已复核通过,3-已复核不通过) */
    @Schema(description = "状态(0-待整改,1-已整改重报,2-已复核通过,3-已复核不通过)")
    private Integer rectificationStatus;

    /** 整改截止时间 */
    @Schema(description = "整改截止时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;

    /** 整改/重报内容说明 */
    @Schema(description = "整改/重报内容说明")
    private String rectifyContent;

    /** 重报时间 */
    @Schema(description = "重报时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date rectifyTime;

    /** 复核结论 */
    @Schema(description = "复核结论")
    private String reviewResult;

    /** 复核人 */
    @Schema(description = "复核人")
    private String reviewer;

    /** 复核时间 */
    @Schema(description = "复核时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
}

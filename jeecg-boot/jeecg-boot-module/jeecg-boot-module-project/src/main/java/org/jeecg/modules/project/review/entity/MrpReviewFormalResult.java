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
 * @Description: 形式审查结果
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_review_formal_result")
public class MrpReviewFormalResult extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 规则ID */
    @Schema(description = "规则ID")
    private String ruleId;

    /** 规则名称快照 */
    @Schema(description = "规则名称快照")
    private String ruleName;

    /** 结果(1-通过,0-不通过) */
    @Schema(description = "结果(1-通过,0-不通过)")
    private Integer checkResult;

    /** 问题描述 */
    @Schema(description = "问题描述")
    private String checkDetail;

    /** 预审时间 */
    @Schema(description = "预审时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /** 审查人（系统自动） */
    @Schema(description = "审查人（系统自动）")
    private String checker;
}

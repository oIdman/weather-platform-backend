package org.jeecg.modules.project.execution.midcheck.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 中期检查评审（Flowable 中检评审流程）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_midcheck_review")
public class MrpMidcheckReview extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 中期报告ID */
    @Schema(description = "中期报告ID")
    private String midcheckReportId;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 评审人 */
    @Schema(description = "评审人")
    private String reviewer;

    /** 结果(1-通过,0-退回) */
    @Schema(description = "结果(1-通过,0-退回)")
    private Integer reviewResult;

    /** 评审意见 */
    @Schema(description = "评审意见")
    private String reviewOpinion;

    /** 评审时间 */
    @Schema(description = "评审时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** Flowable 流程实例ID */
    @Schema(description = "Flowable 流程实例ID")
    private String processInstanceId;
}

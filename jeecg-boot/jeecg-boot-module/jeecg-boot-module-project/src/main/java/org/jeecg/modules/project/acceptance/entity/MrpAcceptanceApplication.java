package org.jeecg.modules.project.acceptance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 验收申请（AI 预验收 / 通过率预测为占位）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_acceptance_application")
public class MrpAcceptanceApplication extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 验收申请编号 */
    @Schema(description = "验收申请编号")
    private String applicationNo;

    /** 验收材料清单（材料梳理） */
    @Schema(description = "验收材料清单（材料梳理）")
    private String materialList;

    /** 状态(0-草稿,1-已提交,2-初审中,3-待专家评审,4-评审中,5-已通过,6-不合格,7-已延期) */
    @Schema(description = "状态(0-草稿,1-已提交,2-初审中,3-待专家评审,4-评审中,5-已通过,6-不合格,7-已延期)")
    private Integer status;

    /** AI 预验收说明（占位） */
    @Schema(description = "AI 预验收说明（占位）")
    private String preAcceptAi;

    /** 通过率预测（占位） */
    @Schema(description = "通过率预测（占位）")
    private String passRatePrediction;

    /** 申请人 */
    @Schema(description = "申请人")
    private String applyBy;

    /** 申请时间 */
    @Schema(description = "申请时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 初审人 */
    @Schema(description = "初审人")
    private String preReviewer;

    /** 初审意见 */
    @Schema(description = "初审意见")
    private String preReviewOpinion;

    /** 初审时间 */
    @Schema(description = "初审时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date preReviewTime;

    /** 验收完成时间 */
    @Schema(description = "验收完成时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 验收结论 */
    @Schema(description = "验收结论")
    private String conclusion;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

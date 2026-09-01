package org.jeecg.modules.project.approval.entity;

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
 * @Description: 项目任务书（人工起草 / 上传 / 审批；智能生成等 AI 子项二期占位）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_task_book")
public class MrpTaskBook extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 任务书编号 */
    @Schema(description = "任务书编号")
    private String taskBookNo;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 任务书标题 */
    @Schema(description = "任务书标题")
    private String title;

    /** 任务书内容（人工起草） */
    @Schema(description = "任务书内容（人工起草）")
    private String content;

    /** 任务书经费(万元) */
    @Schema(description = "任务书经费(万元)")
    private BigDecimal budget;

    /** 状态(0-草稿,1-待审批,2-已通过,3-已退回) */
    @Schema(description = "状态(0-草稿,1-待审批,2-已通过,3-已退回)")
    private Integer status;

    /** 审批人 */
    @Schema(description = "审批人")
    private String approver;

    /** 审批时间 */
    @Schema(description = "审批时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    @Schema(description = "审批意见")
    private String approveOpinion;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

package org.jeecg.modules.project.execution.midcheck.entity;

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
 * @Description: 中期检查报告（提交时自动查重 vs 任务书，非 AI 文本相似度）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_midcheck_report")
public class MrpMidcheckReport extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 报告编号 */
    @Schema(description = "报告编号")
    private String reportNo;

    /** 报告标题 */
    @Schema(description = "报告标题")
    private String reportTitle;

    /** 中期报告正文 */
    @Schema(description = "中期报告正文")
    private String content;

    /** 查重率(%)(vs 任务书) */
    @Schema(description = "查重率(%)(vs 任务书)")
    private BigDecimal similarRate;

    /** 查重说明 */
    @Schema(description = "查重说明")
    private String similarDetail;

    /** 状态(0-草稿,1-已提交,2-评审中,3-已通过,4-已退回) */
    @Schema(description = "状态(0-草稿,1-已提交,2-评审中,3-已通过,4-已退回)")
    private Integer status;

    /** 提交时间 */
    @Schema(description = "提交时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 提交人 */
    @Schema(description = "提交人")
    private String submitBy;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

package org.jeecg.modules.project.execution.progress.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 项目进展报告
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_progress_report")
public class MrpProgressReport extends MrpBaseEntity {

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

    /** 报告期（如 2026Q1 / 第3期） */
    @Schema(description = "报告期（如 2026Q1 / 第3期）")
    private String reportPeriod;

    /** 进展内容 */
    @Schema(description = "进展内容")
    private String content;

    /** 完成度(%)0-100 */
    @Schema(description = "完成度(%)0-100")
    private Integer progressPercent;

    /** 状态(0-草稿,1-已提交) */
    @Schema(description = "状态(0-草稿,1-已提交)")
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

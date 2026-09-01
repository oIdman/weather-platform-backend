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
 * @Description: 立项项目主表（贯穿执行 / 验收 / 归档全流程）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_project")
public class MrpProject extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目编号 */
    @Schema(description = "项目编号")
    private String projectNo;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 项目名称 */
    @Schema(description = "项目名称")
    private String projectName;

    /** 项目类型（字典 project_type） */
    @Schema(description = "项目类型（字典 project_type）")
    private String projectType;

    /** 研究方向 */
    @Schema(description = "研究方向")
    private String researchField;

    /** 负责人ID（科研人员） */
    @Schema(description = "负责人ID（科研人员）")
    private String leaderId;

    /** 负责人姓名快照 */
    @Schema(description = "负责人姓名快照")
    private String leaderName;

    /** 负责人机构快照 */
    @Schema(description = "负责人机构快照")
    private String leaderOrg;

    /** 立项经费(万元) */
    @Schema(description = "立项经费(万元)")
    private BigDecimal budget;

    /** 计划周期(月) */
    @Schema(description = "计划周期(月)")
    private Integer durationMonths;

    /** 计划开始日期 */
    @Schema(description = "计划开始日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 计划结束日期 */
    @Schema(description = "计划结束日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 项目摘要快照 */
    @Schema(description = "项目摘要快照")
    private String summary;

    /** 状态(0-待公示,1-公示中,2-已立项,3-执行中,4-待验收,5-已验收,6-已归档,7-已终止) */
    @Schema(description = "状态(0-待公示,1-公示中,2-已立项,3-执行中,4-待验收,5-已验收,6-已归档,7-已终止)")
    private Integer status;

    /** 立项时间 */
    @Schema(description = "立项时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

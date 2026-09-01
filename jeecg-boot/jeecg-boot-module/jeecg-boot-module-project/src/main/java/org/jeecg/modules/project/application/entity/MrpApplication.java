package org.jeecg.modules.project.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 课题申报主表
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_application")
public class MrpApplication extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 指南标题快照 */
    @Excel(name = "指南标题", width = 30)
    @Schema(description = "指南标题快照")
    private String guideTitle;

    /** 申请人ID（mrp_researcher.id） */
    @Schema(description = "申请人ID（mrp_researcher.id）")
    private String applicantId;

    /** 申请人姓名快照 */
    @Excel(name = "申请人", width = 15)
    @Schema(description = "申请人姓名快照")
    private String applicantName;

    /** 申请人机构快照 */
    @Schema(description = "申请人机构快照")
    private String applicantOrg;

    /** 申请人职称快照 */
    @Schema(description = "申请人职称快照")
    private String applicantTitle;

    /** 项目类型（字典 project_type） */
    @Schema(description = "项目类型（字典 project_type）")
    private String projectType;

    /** 研究方向（字典 research_field） */
    @Schema(description = "研究方向（字典 research_field）")
    private String researchField;

    /** 项目名称 */
    @Excel(name = "项目名称", width = 30)
    @Schema(description = "项目名称")
    private String projectTitle;

    /** 研究方案/摘要 */
    @Schema(description = "研究方案/摘要")
    private String projectSummary;

    /** 申请经费(万元) */
    @Excel(name = "申请经费(万元)", width = 15)
    @Schema(description = "申请经费(万元)")
    private BigDecimal budget;

    /** 计划周期(月) */
    @Excel(name = "计划周期(月)", width = 12)
    @Schema(description = "计划周期(月)")
    private Integer durationMonths;

    /** 状态(0-草稿,1-已提交,2-形式审查中,3-专家评审中,4-评审通过,5-已拒绝,6-整改中) */
    @Excel(name = "状态", width = 10)
    @Schema(description = "状态(0-草稿,1-已提交,2-形式审查中,3-专家评审中,4-评审通过,5-已拒绝,6-整改中)")
    private Integer status;

    /** 提交时间 */
    @Schema(description = "提交时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

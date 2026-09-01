package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 课题指南主表
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide")
public class MrpGuide extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南标题 */
    @Excel(name = "指南标题", width = 30)
    @Schema(description = "指南标题")
    private String guideTitle;

    /** 指南年度 */
    @Excel(name = "指南年度", width = 12)
    @Schema(description = "指南年度")
    private String guideYear;

    /** 指南阶段/批次 */
    @Excel(name = "指南阶段", width = 15)
    @Schema(description = "指南阶段/批次")
    private String stage;

    /** 项目类型（字典 project_type） */
    @Schema(description = "项目类型（字典 project_type）")
    private String projectType;

    /** 状态(0-草稿,1-待发布,2-已发布,3-已归档) */
    @Excel(name = "状态", width = 10)
    @Schema(description = "状态(0-草稿,1-待发布,2-已发布,3-已归档)")
    private Integer status;

    /** 发布范围(all-全员,org-按机构) */
    @Schema(description = "发布范围(all-全员,org-按机构)")
    private String publishScope;

    /** 指南正文 */
    @Schema(description = "指南正文")
    private String content;

    /** 发布人 */
    @Schema(description = "发布人")
    private String publisher;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 归档时间 */
    @Schema(description = "归档时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

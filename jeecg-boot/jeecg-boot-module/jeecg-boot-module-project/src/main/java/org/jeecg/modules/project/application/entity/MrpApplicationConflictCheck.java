package org.jeecg.modules.project.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 申报冲突预检记录
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_application_conflict_check")
public class MrpApplicationConflictCheck extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 申请人ID */
    @Schema(description = "申请人ID")
    private String applicantId;

    /** 预检项（ongoing_project_count/title_qualification/overdue_restricted） */
    @Schema(description = "预检项（ongoing_project_count/title_qualification/overdue_restricted）")
    private String checkType;

    /** 结果(1-通过,0-不通过) */
    @Schema(description = "结果(1-通过,0-不通过)")
    private Integer checkResult;

    /** 明细/原因 */
    @Schema(description = "明细/原因")
    private String checkDetail;

    /** 预检时间 */
    @Schema(description = "预检时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    /** 预检执行方（系统） */
    @Schema(description = "预检执行方（系统）")
    private String checker;
}

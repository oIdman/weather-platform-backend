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
 * @Description: 整改任务（看板，中检退回时自动生成）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_rectification_task")
public class MrpRectificationTask extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 来源评审ID（中检退回生成） */
    @Schema(description = "来源评审ID（中检退回生成）")
    private String midcheckReviewId;

    /** 整改任务标题 */
    @Schema(description = "整改任务标题")
    private String taskTitle;

    /** 任务说明 */
    @Schema(description = "任务说明")
    private String taskDesc;

    /** 责任人 */
    @Schema(description = "责任人")
    private String assignee;

    /** 整改期限 */
    @Schema(description = "整改期限")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;

    /** 状态(0-待处理,1-处理中,2-已完成,3-已关闭) */
    @Schema(description = "状态(0-待处理,1-处理中,2-已完成,3-已关闭)")
    private Integer status;

    /** 处理说明 */
    @Schema(description = "处理说明")
    private String handleContent;

    /** 处理时间 */
    @Schema(description = "处理时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

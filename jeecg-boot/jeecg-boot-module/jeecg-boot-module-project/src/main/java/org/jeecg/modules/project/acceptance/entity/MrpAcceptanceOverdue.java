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
 * @Description: 验收延期处理记录（惩罚机制，供 Phase 2 冲突预检读取受限名单）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_acceptance_overdue")
public class MrpAcceptanceOverdue extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 项目名称快照 */
    @Schema(description = "项目名称快照")
    private String projectName;

    /** 应验收时间 */
    @Schema(description = "应验收时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;

    /** 逾期天数 */
    @Schema(description = "逾期天数")
    private Integer overdueDays;

    /** 状态(0-处理中,1-已通报,2-已限制申报,3-已整改销号) */
    @Schema(description = "状态(0-处理中,1-已通报,2-已限制申报,3-已整改销号)")
    private Integer status;

    /** 处理结果 */
    @Schema(description = "处理结果")
    private String handleResult;

    /** 处理时间 */
    @Schema(description = "处理时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

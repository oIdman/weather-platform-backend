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
 * @Description: 验收会议（会议室 / 设备预约）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_acceptance_meeting")
public class MrpAcceptanceMeeting extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 验收申请ID */
    @Schema(description = "验收申请ID")
    private String acceptanceApplicationId;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 会议名称 */
    @Schema(description = "会议名称")
    private String meetingName;

    /** 会议时间 */
    @Schema(description = "会议时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date meetingTime;

    /** 会议室 */
    @Schema(description = "会议室")
    private String location;

    /** 设备（投影/录播等） */
    @Schema(description = "设备（投影/录播等）")
    private String equipment;

    /** 状态(0-待组织,1-已预约,2-已召开,3-已取消) */
    @Schema(description = "状态(0-待组织,1-已预约,2-已召开,3-已取消)")
    private Integer status;

    /** 组织人 */
    @Schema(description = "组织人")
    private String organizer;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

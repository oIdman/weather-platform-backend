package org.jeecg.modules.project.approval.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 立项异议记录
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_approval_objection")
public class MrpApprovalObjection extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 异议编号 */
    @Schema(description = "异议编号")
    private String objectionNo;

    /** 公示ID */
    @Schema(description = "公示ID")
    private String publicityId;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 异议人 */
    @Schema(description = "异议人")
    private String objectorName;

    /** 异议人机构 */
    @Schema(description = "异议人机构")
    private String objectorOrg;

    /** 异议内容 */
    @Schema(description = "异议内容")
    private String objectionContent;

    /** 状态(0-待核查,1-核查中,2-异议成立,3-异议不成立) */
    @Schema(description = "状态(0-待核查,1-核查中,2-异议成立,3-异议不成立)")
    private Integer status;

    /** 核查结论 */
    @Schema(description = "核查结论")
    private String checkResult;

    /** 核查人 */
    @Schema(description = "核查人")
    private String checker;

    /** 核查时间 */
    @Schema(description = "核查时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;
}

package org.jeecg.modules.project.execution.change.entity;

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
 * @Description: 项目变更申请（影响评估规则）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_change_request")
public class MrpChangeRequest extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 变更编号 */
    @Schema(description = "变更编号")
    private String changeNo;

    /** 变更类型（字典 change_type） */
    @Schema(description = "变更类型（字典 change_type）")
    private String changeType;

    /** 变更标题 */
    @Schema(description = "变更标题")
    private String changeTitle;

    /** 变更内容 */
    @Schema(description = "变更内容")
    private String changeContent;

    /** 预算变动金额(万元,0-无) */
    @Schema(description = "预算变动金额(万元,0-无)")
    private BigDecimal budgetChange;

    /** 影响等级(high-高风险,low-低风险)（规则评估） */
    @Schema(description = "影响等级(high-高风险,low-低风险)（规则评估）")
    private String impactLevel;

    /** 状态(0-草稿,1-待审批,2-已通过,3-已拒绝) */
    @Schema(description = "状态(0-草稿,1-待审批,2-已通过,3-已拒绝)")
    private Integer status;

    /** 申请人 */
    @Schema(description = "申请人")
    private String applyBy;

    /** 申请时间 */
    @Schema(description = "申请时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 审批人 */
    @Schema(description = "审批人")
    private String approver;

    /** 审批时间 */
    @Schema(description = "审批时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    @Schema(description = "审批意见")
    private String approveOpinion;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

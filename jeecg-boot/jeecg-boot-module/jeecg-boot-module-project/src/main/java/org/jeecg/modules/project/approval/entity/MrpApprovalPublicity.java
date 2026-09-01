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
 * @Description: 立项公示记录
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_approval_publicity")
public class MrpApprovalPublicity extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 公示编号 */
    @Schema(description = "公示编号")
    private String publicityNo;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 公示标题/项目名称快照 */
    @Schema(description = "公示标题/项目名称快照")
    private String projectName;

    /** 公示开始时间 */
    @Schema(description = "公示开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 公示结束时间 */
    @Schema(description = "公示结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 状态(0-公示中,1-已结束,2-已撤销) */
    @Schema(description = "状态(0-公示中,1-已结束,2-已撤销)")
    private Integer status;

    /** 公示结论(0-无异议,1-异议处理中,2-有成立异议) */
    @Schema(description = "公示结论(0-无异议,1-异议处理中,2-有成立异议)")
    private Integer result;
}

package org.jeecg.modules.project.archive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 归档操作日志（查阅 / 下载 / 打印）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_archive_log")
public class MrpArchiveLog extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 归档ID */
    @Schema(description = "归档ID")
    private String archiveId;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 操作类型（view-查阅,download-下载,print-打印） */
    @Schema(description = "操作类型（view-查阅,download-下载,print-打印）")
    private String actionType;

    /** 操作人 */
    @Schema(description = "操作人")
    private String operator;

    /** 操作时间 */
    @Schema(description = "操作时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

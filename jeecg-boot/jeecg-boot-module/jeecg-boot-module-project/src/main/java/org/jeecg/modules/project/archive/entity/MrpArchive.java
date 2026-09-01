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
 * @Description: 项目归档主表（归档完成后 Mock 推送数字档案系统）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_archive")
public class MrpArchive extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 归档编号 */
    @Schema(description = "归档编号")
    private String archiveNo;

    /** 归档标题 */
    @Schema(description = "归档标题")
    private String archiveTitle;

    /** 项目编号快照 */
    @Schema(description = "项目编号快照")
    private String projectNo;

    /** 项目名称快照 */
    @Schema(description = "项目名称快照")
    private String projectName;

    /** 归档类别（项目类型快照） */
    @Schema(description = "归档类别（项目类型快照）")
    private String category;

    /** 状态(0-准备中,1-已归档,2-已撤销) */
    @Schema(description = "状态(0-准备中,1-已归档,2-已撤销)")
    private Integer status;

    /** 归档电子档案包(JSON) */
    @Schema(description = "归档电子档案包(JSON)")
    private String packageJson;

    /** 归档时间 */
    @Schema(description = "归档时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    /** 归档人 */
    @Schema(description = "归档人")
    private String archiveBy;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

package org.jeecg.modules.project.archive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 归档材料明细（全流程材料标准化归档）
 * @Author: meteo-project
 * @Date: 2026-08-26
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_archive_item")
public class MrpArchiveItem extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 归档ID */
    @Schema(description = "归档ID")
    private String archiveId;

    /** 项目ID */
    @Schema(description = "项目ID")
    private String projectId;

    /** 材料类型（application/task_book/midcheck/progress/acceptance/achievement/other） */
    @Schema(description = "材料类型（application/task_book/midcheck/progress/acceptance/achievement/other）")
    private String itemType;

    /** 材料名称 */
    @Schema(description = "材料名称")
    private String itemName;

    /** 来源业务ID（如申报/任务书/报告ID） */
    @Schema(description = "来源业务ID（如申报/任务书/报告ID）")
    private String sourceId;

    /** 附件ID（mrp_attachment.id，可空） */
    @Schema(description = "附件ID（mrp_attachment.id，可空）")
    private String fileId;

    /** 附件地址（可空） */
    @Schema(description = "附件地址（可空）")
    private String fileUrl;

    /** 附件大小(字节) */
    @Schema(description = "附件大小(字节)")
    private Long fileSize;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

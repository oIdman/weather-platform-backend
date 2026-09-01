package org.jeecg.modules.project.common.attachment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 公共附件表（多态：biz_type + biz_id）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_attachment")
public class MrpAttachment extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业务类型（见 AttachmentBizTypeEnum） */
    @Schema(description = "业务类型")
    private String bizType;

    /** 业务主键 */
    @Schema(description = "业务主键")
    private String bizId;

    /** 文件名称 */
    @Schema(description = "文件名称")
    private String fileName;

    /** 文件地址 */
    @Schema(description = "文件地址")
    private String fileUrl;

    /** 文件扩展名 */
    @Schema(description = "文件扩展名")
    private String fileType;

    /** 文件大小(字节) */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /** 上传人 */
    @Schema(description = "上传人")
    private String uploadBy;
}

package org.jeecg.modules.project.guide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 指南解读答疑
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_guide_qa")
public class MrpGuideQa extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 指南ID */
    @Schema(description = "指南ID")
    private String guideId;

    /** 问题 */
    @Schema(description = "问题")
    private String question;

    /** 解答 */
    @Schema(description = "解答")
    private String answer;

    /** 提问人 */
    @Schema(description = "提问人")
    private String questioner;

    /** 解答人 */
    @Schema(description = "解答人")
    private String answerer;

    /** 状态(0-待解答,1-已解答) */
    @Schema(description = "状态(0-待解答,1-已解答)")
    private Integer status;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;
}

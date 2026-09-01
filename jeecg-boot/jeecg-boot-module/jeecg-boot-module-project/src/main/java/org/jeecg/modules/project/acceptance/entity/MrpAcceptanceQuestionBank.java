package org.jeecg.modules.project.acceptance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 验收问题库（专家评审辅助）
 * @Author: meteo-project
 * @Date: 2026-08-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_acceptance_question_bank")
public class MrpAcceptanceQuestionBank extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 问题 */
    @Schema(description = "问题")
    private String question;

    /** 参考答案 */
    @Schema(description = "参考答案")
    private String answer;

    /** 分类/领域 */
    @Schema(description = "分类/领域")
    private String category;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

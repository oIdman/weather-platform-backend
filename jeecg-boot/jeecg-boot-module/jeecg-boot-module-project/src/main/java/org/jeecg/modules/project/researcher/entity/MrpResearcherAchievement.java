package org.jeecg.modules.project.researcher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 科研人员关联成果（论文 / 专利 / 软著 / 奖励，支撑申报自动带入既往成果）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_researcher_achievement")
public class MrpResearcherAchievement extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 科研人员ID */
    @Schema(description = "科研人员ID")
    private String researcherId;

    /** 成果类型（论文/专利/软著/奖励） */
    @Schema(description = "成果类型（论文/专利/软著/奖励）")
    private String achievementType;

    /** 成果名称 */
    @Schema(description = "成果名称")
    private String achievementTitle;

    /** 成果日期 */
    @Schema(description = "成果日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date publishDate;

    /** 来源（期刊 / 授权机构等） */
    @Schema(description = "来源（期刊/授权机构等）")
    private String source;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

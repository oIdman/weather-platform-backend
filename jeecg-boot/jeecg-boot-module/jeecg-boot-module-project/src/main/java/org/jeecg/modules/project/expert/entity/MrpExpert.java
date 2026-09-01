package org.jeecg.modules.project.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 专家库（最小化脚手架，审查 / 验收依赖）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_expert")
public class MrpExpert extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 姓名 */
    @Excel(name = "姓名", width = 15)
    @Schema(description = "姓名")
    private String name;

    /** 所属机构 */
    @Excel(name = "所属机构", width = 20)
    @Schema(description = "所属机构")
    private String orgName;

    /** 研究领域 */
    @Excel(name = "研究领域", width = 20)
    @Schema(description = "研究领域")
    private String researchField;

    /** 专家星级(1-5) */
    @Excel(name = "专家星级", width = 10)
    @Schema(description = "专家星级(1-5)")
    private Integer starLevel;

    /** 回避单位（自动检测单位关联） */
    @Schema(description = "回避单位")
    private String avoidOrg;

    /** 联系电话 */
    @Schema(description = "联系电话")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;
}

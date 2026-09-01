package org.jeecg.modules.project.researcher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 科研人员库（最小化脚手架，申报"自动带入既往成果"依赖）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_researcher")
public class MrpResearcher extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 姓名 */
    @Excel(name = "姓名", width = 15)
    @Schema(description = "姓名")
    private String name;

    /** 工号 */
    @Excel(name = "工号", width = 15)
    @Schema(description = "工号")
    private String employeeNo;

    /** 所属机构 */
    @Excel(name = "所属机构", width = 20)
    @Schema(description = "所属机构")
    private String orgName;

    /** 职称 */
    @Excel(name = "职称", width = 15)
    @Schema(description = "职称")
    private String title;

    /** 研究方向 */
    @Excel(name = "研究方向", width = 20)
    @Schema(description = "研究方向")
    private String researchField;

    /** 在研项目数 */
    @Excel(name = "在研项目数", width = 10)
    @Schema(description = "在研项目数")
    private Integer ongoingProjectCount;

    /** 联系电话 */
    @Schema(description = "联系电话")
    private String phone;

    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;
}

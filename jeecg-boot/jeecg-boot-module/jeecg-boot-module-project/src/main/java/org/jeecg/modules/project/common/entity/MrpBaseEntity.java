package org.jeecg.modules.project.common.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @Description: 项目模块实体基类（统一审计字段：机构编码 + 逻辑删除）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MrpBaseEntity extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /** 机构编码 */
    @Schema(description = "机构编码")
    private String sysOrgCode;

    /** 删除状态(0-正常,1-已删除) */
    @Schema(description = "删除状态(0-正常,1-已删除)")
    @TableLogic
    private Integer delFlag;
}

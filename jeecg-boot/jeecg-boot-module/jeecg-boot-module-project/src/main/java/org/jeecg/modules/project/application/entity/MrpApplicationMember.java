package org.jeecg.modules.project.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecg.modules.project.common.entity.MrpBaseEntity;

/**
 * @Description: 申报团队成员
 * @Author: meteo-project
 * @Date: 2026-08-22
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mrp_application_member")
public class MrpApplicationMember extends MrpBaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申报ID */
    @Schema(description = "申报ID")
    private String applicationId;

    /** 科研人员ID（mrp_researcher.id） */
    @Schema(description = "科研人员ID（mrp_researcher.id）")
    private String researcherId;

    /** 成员姓名快照 */
    @Schema(description = "成员姓名快照")
    private String memberName;

    /** 角色（leader-负责人,member-参与人） */
    @Schema(description = "角色（leader-负责人,member-参与人）")
    private String role;

    /** 机构快照 */
    @Schema(description = "机构快照")
    private String orgName;

    /** 职称快照 */
    @Schema(description = "职称快照")
    private String title;

    /** 排序 */
    @Schema(description = "排序")
    private Integer sortOrder;
}

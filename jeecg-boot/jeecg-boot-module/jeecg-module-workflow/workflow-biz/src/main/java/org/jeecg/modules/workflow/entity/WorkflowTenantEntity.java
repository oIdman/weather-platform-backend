package org.jeecg.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class WorkflowTenantEntity {
    private String creator;
    private Date createTime;
    private String updater;
    private Date updateTime;

    @TableLogic
    private Integer deleted;

    private Long tenantId;
}

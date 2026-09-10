package org.jeecg.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@TableName(value = "bpm_user_group", autoResultMap = true)
public class WorkflowUserGroup extends WorkflowTenantEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> userIds;

    private Integer status;
}

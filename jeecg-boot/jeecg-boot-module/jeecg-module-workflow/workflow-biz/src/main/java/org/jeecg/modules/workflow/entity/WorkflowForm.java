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
@TableName(value = "bpm_form", autoResultMap = true)
public class WorkflowForm extends WorkflowTenantEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;
    private String conf;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fields;

    private String remark;
}

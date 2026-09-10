package org.jeecg.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("bpm_process_expression")
public class WorkflowProcessExpression extends WorkflowTenantEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;
    private String expression;
}

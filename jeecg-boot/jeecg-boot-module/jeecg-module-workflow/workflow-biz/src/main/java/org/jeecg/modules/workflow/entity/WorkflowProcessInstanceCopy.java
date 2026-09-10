package org.jeecg.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("bpm_process_instance_copy")
public class WorkflowProcessInstanceCopy extends WorkflowTenantEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String startUserId;
    private String processInstanceId;
    private String processInstanceName;
    private String processDefinitionId;
    private String category;
    private String activityId;
    private String activityName;
    private String taskId;
    private String reason;
}

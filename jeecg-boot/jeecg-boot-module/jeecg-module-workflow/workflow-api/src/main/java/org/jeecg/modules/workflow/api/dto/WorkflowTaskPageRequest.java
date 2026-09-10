package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Task page query aligned with Yudao's BpmTaskPageReqVO.
 */
@Data
public class WorkflowTaskPageRequest {

    @Min(1)
    private Integer pageNo = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String name;
    private String category;
    private String processDefinitionKey;
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;

    @AssertTrue(message = "创建时间范围必须包含按顺序排列的开始和结束时间")
    public boolean isCreateTimeRangeValid() {
        return createTime == null || createTime.length == 0
                || createTime.length == 2 && createTime[0] != null && createTime[1] != null
                && !createTime[0].isAfter(createTime[1]);
    }
}

package org.jeecg.modules.workflow.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Process instance page query aligned with Yudao's BpmProcessInstancePageReqVO.
 */
@Data
public class WorkflowProcessInstancePageRequest {

    @Min(1)
    private Integer pageNo = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String name;
    private String processDefinitionKey;

    @Min(1)
    @Max(4)
    private Integer status;

    private String category;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] endTime;

    /** JeecgBoot user identifiers are strings, unlike Yudao's numeric identifiers. */
    private String startUserId;

    @Size(max = 10000)
    private String formFieldsParams;

    @AssertTrue(message = "创建时间范围必须包含按顺序排列的开始和结束时间")
    public boolean isCreateTimeRangeValid() {
        return isValidRange(createTime);
    }

    @AssertTrue(message = "结束时间范围必须包含按顺序排列的开始和结束时间")
    public boolean isEndTimeRangeValid() {
        return isValidRange(endTime);
    }

    private boolean isValidRange(LocalDateTime[] range) {
        return range == null || range.length == 0
                || range.length == 2 && range[0] != null && range[1] != null && !range[0].isAfter(range[1]);
    }
}

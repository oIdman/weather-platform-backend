package org.jeecg.modules.project.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description: 专家评审提交 VO（多维度打分 + 总体意见）
 * @Author: meteo-project
 * @Date: 2026-08-24
 * @Version: V1.0
 */
@Data
public class MrpReviewScoreSubmitVO {

    @Schema(description = "分配ID")
    private String assignmentId;

    @Schema(description = "申报ID")
    private String applicationId;

    @Schema(description = "专家ID")
    private String expertId;

    @Schema(description = "评审开始时间")
    private Date reviewStartTime;

    @Schema(description = "评审结束时间")
    private Date reviewEndTime;

    @Schema(description = "各维度打分")
    private List<Item> items;

    @Schema(description = "总体评审意见")
    private String opinion;

    @Data
    public static class Item {
        @Schema(description = "维度（innovation/feasibility/research_basis）")
        private String dimension;

        @Schema(description = "分值")
        private BigDecimal score;
    }
}

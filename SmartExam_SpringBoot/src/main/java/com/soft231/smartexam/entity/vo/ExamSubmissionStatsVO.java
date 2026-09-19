package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试提交统计视图对象 - 包含考试信息和提交统计
 */
@Data
public class ExamSubmissionStatsVO {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer totalScore;
    private String userGroupIds;
    private Long creatorId;
    private Integer totalGroupMembers;
    private Integer submittedCount;
    private Integer gradedCount;
    private Integer ungradedCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

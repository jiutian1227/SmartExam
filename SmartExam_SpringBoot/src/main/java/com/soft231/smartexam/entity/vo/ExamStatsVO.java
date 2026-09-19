package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试统计视图对象 - 包含考试详细信息和提交/成员统计
 */
@Data
public class ExamStatsVO {
    private Long id;
    private String title;
    private String description;
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

package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试记录视图对象 - 包含用户信息和考试信息
 */
@Data
public class ExamRecordVO {
    private Long id;
    private Long examId;
    private Long userId;
    private String username;
    private String realName;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer score;
    private Integer status;
    private String gradingStatus;
    private String examTitle;
    private Integer totalScore;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

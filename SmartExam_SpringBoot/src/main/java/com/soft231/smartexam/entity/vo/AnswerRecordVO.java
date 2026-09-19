package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 答题记录视图对象 - 包含题目信息和考试题目分数
 */
@Data
public class AnswerRecordVO {
    private Long id;
    private Long examRecordId;
    private Long questionId;
    private String userAnswer;
    private Integer score;
    private String comment;
    // 题目信息
    private String content;
    private Integer type;
    private String options;
    private String answer;
    private String analysis;
    // 考试题目分数
    private Integer examScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

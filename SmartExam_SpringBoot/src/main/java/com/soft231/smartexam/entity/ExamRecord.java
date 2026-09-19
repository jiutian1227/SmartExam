package com.soft231.smartexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long userId;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private Integer score;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

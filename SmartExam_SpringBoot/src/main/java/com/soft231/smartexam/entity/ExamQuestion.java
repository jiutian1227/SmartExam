package com.soft231.smartexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_question")
public class ExamQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("exam_id")
    private Long examId;

    @TableField("question_id")
    private Long questionId;

    @TableField("score")
    private Integer score;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

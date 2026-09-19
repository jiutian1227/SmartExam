package com.soft231.smartexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("type")
    private Integer type;

    @TableField("content")
    private String content;

    @TableField("options")
    private String options;

    @TableField("answer")
    private String answer;

    @TableField("analysis")
    private String analysis;

    @TableField("knowledge_point_id")
    private Long knowledgePointId;

    @TableField("creator_id")
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

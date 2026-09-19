package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户组视图对象 - 包含成员数量
 */
@Data
public class UserGroupVO {
    private Long id;
    private String name;
    private String description;
    private String shareCode;
    private Long creatorId;
    private Integer memberCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

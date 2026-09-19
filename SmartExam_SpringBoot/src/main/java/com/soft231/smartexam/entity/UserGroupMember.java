package com.soft231.smartexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_group_member")
public class UserGroupMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userGroupId;

    private Long userId;

    private LocalDateTime joinTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

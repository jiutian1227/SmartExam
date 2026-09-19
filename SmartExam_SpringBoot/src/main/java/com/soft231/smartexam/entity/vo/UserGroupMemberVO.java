package com.soft231.smartexam.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户组成员视图对象 - 包含用户信息
 */
@Data
public class UserGroupMemberVO {
    private Long id;
    private Long userGroupId;
    private Long userId;
    private String username;
    private String realName;
    private LocalDateTime joinTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

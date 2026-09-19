package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.entity.vo.UserGroupMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//用户组成员Mapper接口
//查询成员列表（关联用户信息） selectMembersWithUserByGroupId
@Mapper
public interface UserGroupMemberMapper extends BaseMapper<UserGroupMember> {

    //查询用户组成员（关联用户信息）
    @Select("SELECT " +
            "ugm.id, ugm.user_group_id, ugm.user_id, ugm.join_time, " +
            "u.username, u.real_name, " +
            "ugm.create_time, ugm.update_time " +
            "FROM user_group_member ugm " +
            "LEFT JOIN user u ON ugm.user_id = u.id " +
            "WHERE ugm.user_group_id = #{groupId}")
    List<UserGroupMemberVO> selectMembersWithUserByGroupId(@Param("groupId") Long groupId);
}
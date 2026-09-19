package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.UserGroup;
import com.soft231.smartexam.entity.vo.UserGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//用户组Mapper接口
//查询：全部列表 selectGroupsWithMemberCount / 按创建者 selectGroupsWithMemberCountByCreatorId / 详情 selectGroupWithMemberCountById
@Mapper
public interface UserGroupMapper extends BaseMapper<UserGroup> {

    //查询用户组列表（关联成员数量）
    @Select("SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug")
    List<UserGroupVO> selectGroupsWithMemberCount();

    //查询用户组列表按创建者（关联成员数量）
    @Select("SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug WHERE ug.creator_id = #{creatorId}")
    List<UserGroupVO> selectGroupsWithMemberCountByCreatorId(@Param("creatorId") Long creatorId);

    //查询用户组详情（关联成员数量）
    @Select("SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug WHERE ug.id = #{id}")
    UserGroupVO selectGroupWithMemberCountById(@Param("id") Long id);
}
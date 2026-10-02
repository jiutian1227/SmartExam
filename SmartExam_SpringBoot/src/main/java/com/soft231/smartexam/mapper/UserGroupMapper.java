package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    // ── 数据库真分页版本：关键词过滤下推到SQL，IPage由分页插件改写为带LIMIT的SQL ──

    //分页查询用户组列表（全部）
    @Select("<script>" +
            "SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug " +
            "<if test=\"keyword != null and keyword != ''\">WHERE ug.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY ug.id DESC" +
            "</script>")
    IPage<UserGroupVO> selectGroupsWithMemberCountPage(IPage<UserGroupVO> page, @Param("keyword") String keyword);

    //分页查询用户组列表（按创建者）
    @Select("<script>" +
            "SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug " +
            "WHERE ug.creator_id = #{creatorId} " +
            "<if test=\"keyword != null and keyword != ''\">AND ug.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY ug.id DESC" +
            "</script>")
    IPage<UserGroupVO> selectGroupsWithMemberCountByCreatorIdPage(IPage<UserGroupVO> page,
                                                                  @Param("creatorId") Long creatorId,
                                                                  @Param("keyword") String keyword);

    //查询用户组详情（关联成员数量）
    @Select("SELECT " +
            "ug.id, ug.name, ug.description, ug.share_code, ug.creator_id, " +
            "(SELECT COUNT(*) FROM user_group_member ugm WHERE ugm.user_group_id = ug.id) as member_count, " +
            "ug.create_time, ug.update_time " +
            "FROM user_group ug WHERE ug.id = #{id}")
    UserGroupVO selectGroupWithMemberCountById(@Param("id") Long id);
}
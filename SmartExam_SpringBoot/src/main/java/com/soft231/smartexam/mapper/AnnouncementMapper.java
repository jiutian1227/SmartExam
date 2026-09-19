package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//公告Mapper接口
//查询：按角色查询 selectByRole / 查询全部 selectAll
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    //根据角色获取公告列表
    @Select("SELECT * FROM announcement " +
            "WHERE target_roles IS NULL OR target_roles = '' OR FIND_IN_SET(#{role}, target_roles) " +
            "ORDER BY create_time DESC")
    List<Announcement> selectByRole(@Param("role") Integer role);

    //获取所有公告列表
    @Select("SELECT * FROM announcement ORDER BY create_time DESC")
    List<Announcement> selectAll();
}
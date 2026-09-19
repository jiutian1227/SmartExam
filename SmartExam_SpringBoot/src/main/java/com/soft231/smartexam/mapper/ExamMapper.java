package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.soft231.smartexam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

//考试Mapper接口
//获取用户可参加的考试列表 listForUser
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
    @Select("SELECT DISTINCT e.* FROM exam e " +
            "LEFT JOIN user_group_member ugm ON FIND_IN_SET(ugm.user_group_id, e.user_group_ids) " +
            "WHERE e.user_group_ids IS NULL OR e.user_group_ids = '' OR ugm.user_id = #{userId}")
    IPage<Exam> listForUser(IPage<Exam> page, @Param("userId") Long userId);
}
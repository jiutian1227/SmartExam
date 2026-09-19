package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

//考试题目关联Mapper接口（继承BaseMapper，使用MyBatis-Plus自带方法）
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

}
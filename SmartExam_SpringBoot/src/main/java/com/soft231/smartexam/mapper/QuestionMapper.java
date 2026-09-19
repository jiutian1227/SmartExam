package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

//题目Mapper接口（继承BaseMapper，使用MyBatis-Plus自带方法）
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
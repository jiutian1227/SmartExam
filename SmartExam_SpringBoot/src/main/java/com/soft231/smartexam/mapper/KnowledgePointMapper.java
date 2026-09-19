package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;

//知识点Mapper接口（继承BaseMapper，使用MyBatis-Plus自带方法）
@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {
}
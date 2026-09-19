package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.User;
import org.apache.ibatis.annotations.Mapper;

//用户Mapper接口（继承BaseMapper，使用MyBatis-Plus自带方法）
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
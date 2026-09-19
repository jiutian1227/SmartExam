package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.mapper.QuestionMapper;
import com.soft231.smartexam.service.QuestionService;
import org.springframework.stereotype.Service;

//题目服务实现类（继承BaseMapper，使用MyBatis-Plus自带方法）
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}
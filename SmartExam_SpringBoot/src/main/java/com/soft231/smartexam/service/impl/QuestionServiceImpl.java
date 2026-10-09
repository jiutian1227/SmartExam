package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.mapper.QuestionMapper;
import com.soft231.smartexam.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//题目服务实现类（继承BaseMapper，使用MyBatis-Plus自带方法）
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    //批量入库
    @Transactional
    @Override
    public int batchCreate(List<Question> questions, Long creatorId) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        for (Question q : questions) {
            //id置空
            q.setId(null);
            //创建人以JWT身份为准，不信任前端传参
            q.setCreatorId(creatorId);
        }
        saveBatch(questions);
        return questions.size();
    }
}
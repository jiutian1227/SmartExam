package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.ExamQuestion;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.mapper.ExamQuestionMapper;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

//考试题目关联服务实现类
//题目关联：获取题目列表 getExamQuestions / 添加题目 addQuestionToExam / 更新分数 updateQuestionScore / 移除题目 removeQuestionFromExam
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements ExamQuestionService {

    @Autowired
    private QuestionService questionService;

    //获取考试的题目列表
    @Override
    public List<Map<String, Object>> getExamQuestions(Long examId) {
        List<ExamQuestion> examQuestions = this.lambdaQuery()
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSortOrder)
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question question = questionService.getById(eq.getQuestionId());
            if (question != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", question.getId());
                item.put("type", question.getType());
                item.put("content", question.getContent());
                item.put("options", question.getOptions());
                item.put("answer", question.getAnswer());
                item.put("analysis", question.getAnalysis());
                item.put("score", eq.getScore());
                item.put("examScore", eq.getScore());
                item.put("examQuestionId", eq.getId());
                result.add(item);
            }
        }

        result.sort(Comparator.comparingInt(a -> (Integer) a.get("type")));
        return result;
    }

    //向考试添加题目
    @Override
    public ExamQuestion addQuestionToExam(Long examId, Map<String, Object> body) {
        Long questionId = ((Number) body.get("questionId")).longValue();
        Integer score = body.get("score") != null ? ((Number) body.get("score")).intValue() : 10;
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamId(examId);
        examQuestion.setQuestionId(questionId);
        examQuestion.setScore(score);
        examQuestion.setSortOrder(sortOrder);

        this.save(examQuestion);
        return examQuestion;
    }

    //更新题目在考试中的分数
    @Override
    public void updateQuestionScore(Long examId, Map<String, Object> body) {
        Long examQuestionId = ((Number) body.get("examQuestionId")).longValue();
        Integer score = ((Number) body.get("score")).intValue();

        ExamQuestion examQuestion = this.getById(examQuestionId);
        if (examQuestion != null && examQuestion.getExamId().equals(examId)) {
            examQuestion.setScore(score);
            this.updateById(examQuestion);
        }
    }

    //从考试中移除题目
    @Override
    public boolean removeQuestionFromExam(Long examId, Long questionId) {
        return this.lambdaUpdate()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId)
                .remove();
    }
}
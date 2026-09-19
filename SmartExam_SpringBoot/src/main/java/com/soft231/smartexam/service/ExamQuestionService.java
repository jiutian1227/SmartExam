package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.ExamQuestion;

import java.util.List;
import java.util.Map;

//考试题目关联服务接口
//获取考试的题目列表
//向考试添加题目
//更新题目在考试中的分数
//从考试中移除题目
public interface ExamQuestionService extends IService<ExamQuestion> {

    //获取考试的题目列表
    List<Map<String, Object>> getExamQuestions(Long examId);

    //向考试添加题目
    ExamQuestion addQuestionToExam(Long examId, Map<String, Object> body);

    //更新题目在考试中的分数
    void updateQuestionScore(Long examId, Map<String, Object> body);

    //从考试中移除题目
    boolean removeQuestionFromExam(Long examId, Long questionId);
}
package com.soft231.smartexam.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//AI题目服务接口
//生成题目
//AI批改主观题
public interface AiQuestionService {

    //生成题目
    List<Map<String, Object>> generateQuestions(int type, int count, String requirement, Long knowledgePointId, String knowledgePointName) throws IOException;

    //AI批改主观题
    Map<String, Object> gradeSubjectiveQuestion(String questionContent, String correctAnswer, String studentAnswer, int type, int score) throws IOException;
}
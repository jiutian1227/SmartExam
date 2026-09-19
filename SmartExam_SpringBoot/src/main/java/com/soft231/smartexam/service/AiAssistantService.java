package com.soft231.smartexam.service;

import java.io.InputStream;
import java.util.Map;

//AI助手服务接口
//回答学生问题
//流式回答学生问题
public interface AiAssistantService {

    //回答学生问题
    Map<String, Object> answerQuestion(String question, String subject) throws Exception;

    //流式回答学生问题
    InputStream answerQuestionStream(String question, String subject) throws Exception;
}
package com.soft231.smartexam.service;

import java.util.Map;

//题解服务接口
//AI生成题目解析/题解 generateSolution
public interface SolutionService {

    //生成题解
    //@param questionContent 题目内容
    //@param options 选项（JSON字符串，选择题用）
    //@param correctAnswer 正确答案
    //@param type 题目类型（0单选 1多选 2判断 3填空 4简答）
    //@param studentAnswer 学生答案
    //@param existingAnalysis 数据库中已有的解析（可选）
    //@return 包含题解文本的Map
    Map<String, Object> generateSolution(String questionContent, String options,
                                          String correctAnswer, Integer type,
                                          String studentAnswer, String existingAnalysis);
}

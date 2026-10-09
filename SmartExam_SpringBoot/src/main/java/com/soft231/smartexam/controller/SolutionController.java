package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.service.SolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//题解控制器
//AI生成题解 generate
@RestController
@RequestMapping("/api/solution")
public class SolutionController {

    /** 单个文本入参的最大长度，防止超长内容拼进Prompt刷爆AI资源 */
    private static final int MAX_TEXT_LENGTH = 2000;

    @Autowired
    private SolutionService solutionService;

    //AI生成题解
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody Map<String, Object> request) {
        try {
            String questionContent = (String) request.get("questionContent");
            String options = (String) request.get("options");
            String correctAnswer = (String) request.get("correctAnswer");
            Integer type = request.get("type") != null
                    ? ((Number) request.get("type")).intValue()
                    : null;
            String studentAnswer = (String) request.get("studentAnswer");
            String existingAnalysis = (String) request.get("existingAnalysis");

            if (questionContent == null || questionContent.trim().isEmpty()) {
                return Result.error("题目内容不能为空");
            }
            if (type == null) {
                return Result.error("题目类型不能为空");
            }
            // 该接口开放给全体登录学生查看已批阅成绩时要看解析
            // 入参会直接拼进大模型Prompt，必须限制长度，防止超长文本刷爆AI资源
            if (questionContent.length() > MAX_TEXT_LENGTH) {
                return Result.error("题目内容不能超过" + MAX_TEXT_LENGTH + "字");
            }
            if (options != null && options.length() > MAX_TEXT_LENGTH) {
                return Result.error("选项内容过长");
            }
            if (correctAnswer != null && correctAnswer.length() > MAX_TEXT_LENGTH) {
                return Result.error("参考答案过长");
            }
            if (studentAnswer != null && studentAnswer.length() > MAX_TEXT_LENGTH) {
                return Result.error("作答内容过长");
            }
            if (existingAnalysis != null && existingAnalysis.length() > MAX_TEXT_LENGTH) {
                return Result.error("已有解析过长");
            }

            Map<String, Object> result = solutionService.generateSolution(
                    questionContent, options, correctAnswer, type,
                    studentAnswer, existingAnalysis
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("题解生成失败: " + e.getMessage());
        }
    }
}

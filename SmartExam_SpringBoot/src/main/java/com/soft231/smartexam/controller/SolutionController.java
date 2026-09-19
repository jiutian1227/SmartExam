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

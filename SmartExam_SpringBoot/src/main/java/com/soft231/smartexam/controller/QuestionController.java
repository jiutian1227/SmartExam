package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.service.impl.AiQuestionServiceImpl;
import com.soft231.smartexam.service.KnowledgePointService;
import com.soft231.smartexam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//题目管理控制器
//题目CRUD：创建 create / 查询 getById / 更新 update / 删除 delete / 列表 list
//AI生成题目：AI智能出题 aiGenerate
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiQuestionServiceImpl aiQuestionService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    //创建题目
    @PostMapping
    public Result<Question> create(@RequestBody Question question) {
        questionService.save(question);
        return Result.success(question);
    }

    //根据ID查询题目
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    //更新题目信息
    @PutMapping
    public Result<Question> update(@RequestBody Question question) {
        questionService.updateById(question);
        return Result.success(question);
    }

    //删除题目
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        return Result.success();
    }

    //获取题目列表（分页）
    @GetMapping
    public Result<Page<Question>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) Long creatorId
    ) {
        Page<Question> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Question::getContent, keyword);
        }
        if (knowledgePointId != null) {
            wrapper.eq(Question::getKnowledgePointId, knowledgePointId);
        }
        if (creatorId != null) {
            wrapper.eq(Question::getCreatorId, creatorId);
        }
        
        wrapper.orderByAsc(Question::getId);
        return Result.success(questionService.page(page, wrapper));
    }

    //AI生成题目
    @PostMapping("/ai-generate")
    public Result<List<Map<String, Object>>> aiGenerate(@RequestBody Map<String, Object> request) {
        try {
            int type = ((Number) request.getOrDefault("type", 0)).intValue();
            int count = ((Number) request.getOrDefault("count", 5)).intValue();
            String requirement = (String) request.get("requirement");
            Long knowledgePointId = request.get("knowledgePointId") != null 
                ? ((Number) request.get("knowledgePointId")).longValue() 
                : null;
            
            String knowledgePointName = null;
            if (knowledgePointId != null) {
                KnowledgePoint kp = knowledgePointService.getById(knowledgePointId);
                if (kp != null) {
                    knowledgePointName = kp.getName();
                }
            }
            
            List<Map<String, Object>> questions = aiQuestionService.generateQuestions(type, count, requirement, knowledgePointId, knowledgePointName);
            return Result.success(questions);
        } catch (Exception e) {
            return Result.error("生成题目失败: " + e.getMessage());
        }
    }
}

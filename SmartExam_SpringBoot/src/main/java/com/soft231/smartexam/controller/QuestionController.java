package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.entity.ExamQuestion;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.service.AiQuestionService;
import com.soft231.smartexam.service.AnswerRecordService;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.KnowledgePointService;
import com.soft231.smartexam.service.QuestionService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//题目管理控制器
//题目CRUD：创建 create / 查询 getById / 更新 update / 删除 delete / 列表 list
//AI生成题目：AI智能出题 aiGenerate
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiQuestionService aiQuestionService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private ExamQuestionService examQuestionService;

    @Autowired
    private AnswerRecordService answerRecordService;

    //创建题目
    @PostMapping
    public Result<Question> create(@RequestBody Question question) {
        // creatorId以JWT身份为准，不信任前端传参
        question.setCreatorId(SecurityUtils.getUserId());
        questionService.save(question);
        return Result.success(question);
    }

    //批量创建题目 —— AI出题勾选后一次性落库，避免逐条请求造成"只加了一半"
    //creatorId与id的处理下沉到Service，事务也在Service层，保证整批原子性
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchCreate(@RequestBody List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return Result.error(400, "请先选择要添加的题目");
        }
        int count = questionService.batchCreate(questions, SecurityUtils.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    //根据ID查询题目 —— 含答案与解析，仅教师及以上可见（SecurityConfig已限角色，此处为纵深防御）
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        if (!SecurityUtils.isTeacherOrAbove()) {
            throw new AccessDeniedException("学生无权查看题目答案与解析");
        }
        return Result.success(questionService.getById(id));
    }

    //更新题目信息 —— 教师只能改自己创建的题目，超管不限
    @PutMapping
    public Result<Question> update(@RequestBody Question question) {
        if (!SecurityUtils.isSuperAdmin()) {
            Question existing = questionService.getById(question.getId());
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能修改自己创建的题目");
            }
            question.setCreatorId(existing.getCreatorId());
        }
        questionService.updateById(question);
        return Result.success(question);
    }

    //删除题目 —— 教师只能删自己创建的题目，超管不限
    //删除前做两道引用校验，避免"题目被删、引用它的试卷和成绩还在"的数据错乱：
    // 1) 已被学生作答（answer_record有记录）：外键是ON DELETE RESTRICT，硬删会被数据库拒绝并抛500，提前拦下给明确提示
    // 2) 已被试卷选用（exam_question有关联行）：外键是ON DELETE CASCADE，硬删会让该题从所有试卷中静默消失、总分对不上
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!SecurityUtils.isSuperAdmin()) {
            Question existing = questionService.getById(id);
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能删除自己创建的题目");
            }
        }

        Long answeredCount = answerRecordService.lambdaQuery()
                .eq(AnswerRecord::getQuestionId, id)
                .count();
        if (answeredCount != null && answeredCount > 0) {
            return Result.error(400, "该题已被学生作答 " + answeredCount
                    + " 次，删除会导致成绩无法追溯，禁止删除");
        }

        Long usedCount = examQuestionService.lambdaQuery()
                .eq(ExamQuestion::getQuestionId, id)
                .count();
        if (usedCount != null && usedCount > 0) {
            return Result.error(400, "该题已被 " + usedCount
                    + " 场考试选用，请先将它从这些试卷中移除，再执行删除");
        }

        questionService.removeById(id);
        return Result.success();
    }

    //获取题目列表（分页）—— 教师的creatorId过滤以token身份为准，超管可看全部
    //学生不参与题库管理，SecurityConfig已限教师及以上，此处再兜底拦截一次
    //筛选条件（keyword/knowledgePointId/type/excludeExamId）全部下推到SQL，配合分页保证每页条数准确
    @GetMapping
    public Result<Page<Question>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long excludeExamId
    ) {
        if (!SecurityUtils.isTeacherOrAbove()) {
            throw new AccessDeniedException("学生无权访问题库");
        }
        Page<Question> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Question::getContent, keyword);
        }
        if (knowledgePointId != null) {
            wrapper.eq(Question::getKnowledgePointId, knowledgePointId);
        }
        if (type != null) {
            wrapper.eq(Question::getType, type);
        }
        if (excludeExamId != null) {
            // 试卷装配场景：剔除该考试已选中的题目。若放在前端过滤，会出现"翻页后每页不足pageSize"的问题
            wrapper.apply("NOT EXISTS (SELECT 1 FROM exam_question eq "
                    + "WHERE eq.exam_id = {0} AND eq.question_id = question.id)", excludeExamId);
        }
        Long creatorId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.getUserId();
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

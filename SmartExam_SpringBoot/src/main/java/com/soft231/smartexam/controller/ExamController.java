package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.ExamQuestion;
import com.soft231.smartexam.entity.vo.ExamStatsVO;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.ExamRecordService;
import com.soft231.smartexam.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//考试管理控制器
//考卷CRUD：创建 create / 详情 getById / 更新 update / 删除 delete / 列表 list
//题目关联：获取题目 getExamQuestions / 添加题目 addQuestionToExam / 修改分值 updateQuestionScore / 移除题目 removeQuestionFromExam
//学生入口：可参加的考试列表 listForStudent
//统计数据：考试参与情况统计 getExamStats
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamQuestionService examQuestionService;

    //创建考试
    @PostMapping
    public Result<Exam> create(@RequestBody Exam exam) {
        Exam saved = examService.createExam(exam);
        return Result.success(saved);
    }

    //根据ID查询考试
    @GetMapping("/{id}")
    public Result<Exam> getById(@PathVariable Long id) {
        return Result.success(examService.getById(id));
    }

    //更新考试信息
    @PutMapping
    public Result<Exam> update(@RequestBody Exam exam) {
        examService.updateById(exam);
        return Result.success(exam);
    }

    //删除考试
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.removeById(id);
        return Result.success();
    }

    //获取考试列表（分页）
    @GetMapping
    public Result<Page<Exam>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long creatorId
    ) {
        Page<Exam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Exam::getTitle, keyword);
        }
        if (creatorId != null) {
            wrapper.eq(Exam::getCreatorId, creatorId);
        }

        wrapper.orderByDesc(Exam::getId);
        return Result.success(examService.page(page, wrapper));
    }


    //获取学生可参加的考试列表
    @GetMapping("/available")
    public Result<IPage<Exam>> listForStudent(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "6") Integer pageSize
    ) {
        Page<Exam> page = new Page<>(pageNum, pageSize);
        return Result.success(examService.listForUser(page, userId));
    }


    //获取考试的题目列表
    @GetMapping("/{examId}/questions")
    public Result<?> getExamQuestions(@PathVariable Long examId) {
        List<Map<String, Object>> questions = examQuestionService.getExamQuestions(examId);
        return Result.success(questions);
    }

    //向考试添加题目
    @PostMapping("/{examId}/questions")
    public Result<?> addQuestionToExam(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        ExamQuestion eq = examQuestionService.addQuestionToExam(examId, body);
        return Result.success(Map.of("id", eq.getId()));
    }

    //更新题目在考试中的分数
    @PutMapping("/{examId}/questions/score")
    public Result<?> updateQuestionScore(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        examQuestionService.updateQuestionScore(examId, body);
        return Result.success();
    }

    //从考试中移除题目
    @DeleteMapping("/{examId}/questions/{questionId}")
    public Result<?> removeQuestionFromExam(@PathVariable Long examId, @PathVariable Long questionId) {
        boolean removed = examQuestionService.removeQuestionFromExam(examId, questionId);
        return Result.success(removed);
    }

    //获取考试统计信息
    @GetMapping("/{examId}/stats")
    public Result<ExamStatsVO> getExamStats(@PathVariable Long examId) {
        return Result.success(examRecordService.getExamStats(examId));
    }
}

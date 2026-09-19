package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;
import com.soft231.smartexam.service.AiQuestionService;
import com.soft231.smartexam.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//考试记录 + 批阅管理控制器
//考试流程：开始考试 startExam / 提交考试 submitExam
//成绩查询：我的成绩列表 listByUserId / 记录详情 getById / 本场考试所有提交的试卷列表 getRecordsByExamId
//批阅功能：获取答题详情 getRecordAnswers / 自动判客观题 autoGradeObjectiveQuestions / 提交分数 submitScores
//AI批阅：AI批主观题 aiGradeSubjective
//统计：批阅统计 getGradingStats
@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private AiQuestionService aiQuestionService;


    //创建考试记录
    @PostMapping
    public Result<ExamRecord> create(@RequestBody ExamRecord record) {
        ExamRecord saved = examRecordService.createRecord(record);
        return Result.success(saved);
    }

    //开始考试
    @PostMapping("/start")
    public Result<ExamRecord> startExam(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long examId = Long.valueOf(data.get("examId").toString());
        ExamRecord record = examRecordService.startExam(userId, examId);
        return Result.success(record);
    }

    //提交考试
    @PostMapping("/submit")
    public Result<ExamRecord> submitExam(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        Long examId = Long.valueOf(data.get("examId").toString());
        String answers = data.get("answers") != null ? data.get("answers").toString() : null;
        ExamRecord record = examRecordService.submitExam(userId, examId, answers);
        return Result.success(record);
    }

    //获取所有考试记录列表（分页）
    @GetMapping
    public Result<Page<ExamRecordVO>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "1000") Integer pageSize
    ) {
        List<ExamRecordVO> allData = examRecordService.getAllRecordsWithInfo();
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allData.size());
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        page.setRecords(start < end ? allData.subList(start, end) : new ArrayList<>());
        return Result.success(page);
    }

    //根据ID查询考试记录详情
    @GetMapping("/{id}")
    public Result<ExamRecordVO> getById(@PathVariable Long id) {
        ExamRecordVO vo = examRecordService.getRecordById(id);
        if (vo == null) {
            return Result.error("记录不存在");
        }
        return Result.success(vo);
    }

    //我的成绩列表（根据用户ID）
    @GetMapping("/my/{userId}")
    public Result<Page<ExamRecordVO>> listByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        List<ExamRecordVO> allData = examRecordService.getRecordsWithExamInfoByUserId(userId);
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allData.size());
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        page.setRecords(start < end ? allData.subList(start, end) : new ArrayList<>());
        return Result.success(page);
    }

    //获取用户已提交的考试ID列表
    @GetMapping("/my/{userId}/exam-ids")
    public Result<List<Long>> getUserExamIds(@PathVariable Long userId) {
        List<Long> examIds = examRecordService.getSubmittedExamIdsByUserId(userId);
        return Result.success(examIds);
    }

    //获取用户的考试状态
    @GetMapping("/my/{userId}/exam/{examId}/status")
    public Result<Map<String, Object>> getExamStatus(@PathVariable Long userId, @PathVariable Long examId) {
        Map<String, Object> result = examRecordService.getExamRecordStatus(userId, examId);
        return Result.success(result);
    }

    //获取考试的所有记录（分页）—— 老师批阅时看
    @GetMapping("/by-exam/{examId}")
    public Result<Page<ExamRecordVO>> getRecordsByExamId(
            @PathVariable Long examId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        List<ExamRecordVO> allData = examRecordService.getRecordsByExamId(examId);
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allData.size());
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        page.setRecords(start < end ? allData.subList(start, end) : new ArrayList<>());
        return Result.success(page);
    }

    //获取考试记录的答案列表
    @GetMapping("/{recordId}/answers")
    public Result<List<Map<String, Object>>> getRecordAnswers(@PathVariable Long recordId) {
        return Result.success(examRecordService.getRecordAnswers(recordId));
    }

    //自动批改客观题
    @PostMapping("/{recordId}/auto-grade")
    public Result<List<Map<String, Object>>> autoGradeObjectiveQuestions(@PathVariable Long recordId) {
        return Result.success(examRecordService.autoGradeObjectiveQuestions(recordId));
    }

    //提交分数
    @PostMapping("/{recordId}/scores")
    public Result<Void> submitScores(@PathVariable Long recordId, @RequestBody List<Map<String, Object>> scoreList) {
        examRecordService.submitScores(recordId, scoreList);
        return Result.success();
    }


    //AI批改主观题
    @PostMapping("/ai-grade-subjective")
    public Result<Map<String, Object>> aiGradeSubjective(@RequestBody Map<String, Object> data) {
        try {
            String questionContent = (String) data.get("questionContent");
            String correctAnswer = (String) data.get("correctAnswer");
            String studentAnswer = (String) data.get("studentAnswer");
            Integer type = (Integer) data.get("type");
            Integer score = (Integer) data.get("score");

            if (questionContent == null || correctAnswer == null || studentAnswer == null || type == null || score == null) {
                return Result.error("缺少必要参数");
            }

            Map<String, Object> result = aiQuestionService.gradeSubjectiveQuestion(
                questionContent, correctAnswer, studentAnswer, type, score
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("AI批改失败: " + e.getMessage());
        }
    }

    //批阅统计（分页）
    @GetMapping("/stats")
    public Result<Page<ExamSubmissionStatsVO>> getGradingStats(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long creatorId
    ) {
        List<ExamSubmissionStatsVO> allData = examRecordService.getExamSubmissionStats(creatorId);
        Page<ExamSubmissionStatsVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allData.size());
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allData.size());
        page.setRecords(start < end ? allData.subList(start, end) : new ArrayList<>());
        return Result.success(page);
    }

    //删除考试记录
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examRecordService.removeById(id);
        return Result.success();
    }
}

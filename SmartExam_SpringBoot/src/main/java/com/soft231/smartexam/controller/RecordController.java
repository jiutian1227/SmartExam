package com.soft231.smartexam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;
import com.soft231.smartexam.service.AiQuestionService;
import com.soft231.smartexam.service.ExamRecordService;
import com.soft231.smartexam.service.ExamService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private ExamService examService;

    @Autowired
    private AiQuestionService aiQuestionService;

    //创建考试记录（userId一律取JWT身份，不信任前端传参）
    @PostMapping
    public Result<ExamRecord> create(@RequestBody ExamRecord record) {
        record.setUserId(SecurityUtils.getUserId());
        ExamRecord saved = examRecordService.createRecord(record);
        return Result.success(saved);
    }

    //开始考试 —— 学生只能开始其用户组可见的考试
    @PostMapping("/start")
    public Result<ExamRecord> startExam(@RequestBody Map<String, Object> data) {
        Long userId = SecurityUtils.getUserId();
        Long examId = Long.valueOf(data.get("examId").toString());
        assertExamAccessible(examId);
        ExamRecord record = examRecordService.startExam(userId, examId);
        return Result.success(record);
    }

    //提交考试 —— 同上做试卷可见性校验
    @PostMapping("/submit")
    public Result<ExamRecord> submitExam(@RequestBody Map<String, Object> data) {
        Long userId = SecurityUtils.getUserId();
        Long examId = Long.valueOf(data.get("examId").toString());
        assertExamAccessible(examId);
        String answers = data.get("answers") != null ? data.get("answers").toString() : null;
        ExamRecord record = examRecordService.submitExam(userId, examId, answers);
        return Result.success(record);
    }

    //保存答题草稿（自动存卷）—— 学生作答过程中每 30 秒上报一次
    //刻意不返回完整记录：接口会被高频调用，只回一个成功标识，省流量
    @PostMapping("/draft")
    public Result<Void> saveDraft(@RequestBody Map<String, Object> data) {
        Long userId = SecurityUtils.getUserId();
        Object examIdObj = data.get("examId");
        if (examIdObj == null) {
            throw new IllegalArgumentException("缺少考试ID");
        }
        Long examId = Long.valueOf(examIdObj.toString());
        assertExamAccessible(examId);
        String answers = data.get("answers") != null ? data.get("answers").toString() : null;
        examRecordService.saveDraft(userId, examId, answers);
        return Result.success();
    }

    //获取所有考试记录列表（分页）
    @GetMapping
    public Result<Page<ExamRecordVO>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "1000") Integer pageSize
    ) {
        // 数据库分页：LIMIT下推到SQL，不再全表捞进内存
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        examRecordService.getAllRecordsWithInfo(page);
        return Result.success(page);
    }

    //根据ID查询考试记录详情 —— 学生只能看本人记录，教师只能看自己创建的考试的记录
    @GetMapping("/{id}")
    public Result<ExamRecordVO> getById(@PathVariable Long id) {
        assertRecordVisible(id);
        ExamRecordVO vo = examRecordService.getRecordById(id);
        if (vo == null) {
            return Result.error("记录不存在");
        }
        return Result.success(vo);
    }

    //我的成绩列表（当前登录用户）
    @GetMapping("/my")
    public Result<Page<ExamRecordVO>> listByUserId(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        Long userId = SecurityUtils.getUserId();
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        examRecordService.getRecordsWithExamInfoByUserId(page, userId);
        return Result.success(page);
    }

    //获取当前用户已提交的考试ID列表
    @GetMapping("/my/exam-ids")
    public Result<List<Long>> getUserExamIds() {
        Long userId = SecurityUtils.getUserId();
        List<Long> examIds = examRecordService.getSubmittedExamIdsByUserId(userId);
        return Result.success(examIds);
    }

    //获取当前用户的考试状态
    //withAnswers=true 时额外返回已保存的草稿答案，供页面加载时恢复作答
    @GetMapping("/my/exam/{examId}/status")
    public Result<Map<String, Object>> getExamStatus(
            @PathVariable Long examId,
            @RequestParam(required = false, defaultValue = "false") boolean withAnswers) {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> result = examRecordService.getExamRecordStatus(userId, examId, withAnswers);
        return Result.success(result);
    }

    //获取考试的所有记录（分页）—— 老师批阅时看
    @GetMapping("/by-exam/{examId}")
    public Result<Page<ExamRecordVO>> getRecordsByExamId(
            @PathVariable Long examId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        assertExamOwner(examId);
        Page<ExamRecordVO> page = new Page<>(pageNum, pageSize);
        examRecordService.getRecordsByExamId(page, examId);
        return Result.success(page);
    }

    //获取考试记录的答案列表 —— 归属校验同记录详情
    @GetMapping("/{recordId}/answers")
    public Result<List<Map<String, Object>>> getRecordAnswers(@PathVariable Long recordId) {
        assertRecordVisible(recordId);
        return Result.success(examRecordService.getRecordAnswers(recordId));
    }

    //自动批改客观题 —— 教师/超管，且考试须为自己创建
    @PostMapping("/{recordId}/auto-grade")
    public Result<List<Map<String, Object>>> autoGradeObjectiveQuestions(@PathVariable Long recordId) {
        assertRecordVisible(recordId);
        return Result.success(examRecordService.autoGradeObjectiveQuestions(recordId));
    }

    //提交分数 —— 教师/超管，且考试须为自己创建
    @PostMapping("/{recordId}/scores")
    public Result<Void> submitScores(@PathVariable Long recordId, @RequestBody List<Map<String, Object>> scoreList) {
        assertRecordVisible(recordId);
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

    //批阅统计（分页）—— 教师只看自己创建的考试，超管可看全部
    @GetMapping("/stats")
    public Result<Page<ExamSubmissionStatsVO>> getGradingStats(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        // creatorId以token身份为准，不信任前端传参
        Long creatorId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.getUserId();
        Page<ExamSubmissionStatsVO> page = new Page<>(pageNum, pageSize);
        examRecordService.getExamSubmissionStats(page, creatorId);
        return Result.success(page);
    }

    //删除考试记录 —— 教师/超管，且考试须为自己创建
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assertRecordVisible(id);
        examRecordService.removeById(id);
        return Result.success();
    }

    /**
     * 按JWT身份校验考试归属：超级管理员放行，教师只能操作自己创建的考试
     */
    private void assertExamOwner(Long examId) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Exam exam = examService.getById(examId);
        if (exam == null || !Objects.equals(exam.getCreatorId(), SecurityUtils.getUserId())) {
            throw new AccessDeniedException("无权访问该考试的数据");
        }
    }

    /**
     * 按JWT身份校验试卷可见性：超级管理员放行；
     * 教师看自己创建的，学生只能参加其所属用户组已绑定的考试
     */
    private void assertExamAccessible(Long examId) {
        Long currentUserId = SecurityUtils.getUserId();
        Integer role = SecurityUtils.getRole();
        if (currentUserId == null || role == null) {
            throw new AccessDeniedException("未登录或登录已过期");
        }
        if (!examService.canAccess(examId, currentUserId, role)) {
            throw new AccessDeniedException("该考试不在你可参加的范围内");
        }
    }

    /**
     * 按JWT身份校验考试记录可见性：
     * 超级管理员放行；本人可看自己的记录；教师可看自己创建的考试的记录
     */
    private void assertRecordVisible(Long recordId) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtils.getUserId();
        ExamRecord record = examRecordService.getById(recordId);
        if (record == null) {
            throw new AccessDeniedException("记录不存在");
        }
        if (Objects.equals(record.getUserId(), currentUserId)) {
            return;
        }
        if (SecurityUtils.isTeacherOrAbove()) {
            Exam exam = examService.getById(record.getExamId());
            if (exam != null && Objects.equals(exam.getCreatorId(), currentUserId)) {
                return;
            }
        }
        throw new AccessDeniedException("无权访问该考试记录");
    }
}

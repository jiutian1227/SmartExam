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
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        // creatorId以JWT身份为准，不信任前端传参
        exam.setCreatorId(SecurityUtils.getUserId());
        // 必须绑定用户组，否则学生无法在自己的考试列表中看到该试卷
        if (exam.getUserGroupIds() == null || exam.getUserGroupIds().trim().isEmpty()) {
            return Result.error(400, "请至少选择一个可见用户组");
        }
        Exam saved = examService.createExam(exam);
        return Result.success(saved);
    }

    //根据ID查询考试 —— 超管放行；教师只能看自己创建的；学生只能看已绑定用户组内的试卷
    @GetMapping("/{id}")
    public Result<Exam> getById(@PathVariable Long id) {
        assertExamAccessible(id);
        return Result.success(examService.getById(id));
    }

    //更新考试信息 —— 教师只能改自己创建的考试，超管不限
    @PutMapping
    public Result<Exam> update(@RequestBody Exam exam) {
        if (!SecurityUtils.isSuperAdmin()) {
            Exam existing = examService.getById(exam.getId());
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能修改自己创建的考试");
            }
            // 防止越权篡改考试归属
            exam.setCreatorId(existing.getCreatorId());
        }
        // 传空串表示清空用户组，会导致学生看不到该试卷
        if (exam.getUserGroupIds() != null && exam.getUserGroupIds().trim().isEmpty()) {
            return Result.error(400, "请至少选择一个可见用户组");
        }
        examService.updateById(exam);
        return Result.success(exam);
    }

    //删除考试 —— 教师只能删自己创建的考试，超管不限
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assertExamOwner(id);
        examService.removeById(id);
        return Result.success();
    }

    //获取考试列表（分页）—— 教师的creatorId过滤以token身份为准，超管可看全部
    @GetMapping
    public Result<Page<Exam>> list(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        Page<Exam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Exam::getTitle, keyword);
        }
        Long creatorId = SecurityUtils.isSuperAdmin() ? null : SecurityUtils.getUserId();
        if (creatorId != null) {
            wrapper.eq(Exam::getCreatorId, creatorId);
        }

        wrapper.orderByDesc(Exam::getId);
        return Result.success(examService.page(page, wrapper));
    }


    //获取当前学生可参加的考试列表
    @GetMapping("/available")
    public Result<IPage<Exam>> listForStudent(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "6") Integer pageSize
    ) {
        Long userId = SecurityUtils.getUserId();
        Page<Exam> page = new Page<>(pageNum, pageSize);
        return Result.success(examService.listForUser(page, userId));
    }


    //获取考试的题目列表 —— 学生答题入口，须先校验试卷可见性
    @GetMapping("/{examId}/questions")
    public Result<?> getExamQuestions(@PathVariable Long examId) {
        assertExamAccessible(examId);
        // 学生作答时不下发答案与解析，仅教师及以上（编辑试卷场景）返回完整字段
        boolean includeAnswer = SecurityUtils.isTeacherOrAbove();
        List<Map<String, Object>> questions = examQuestionService.getExamQuestions(examId, includeAnswer);
        return Result.success(questions);
    }

    //向考试添加题目
    @PostMapping("/{examId}/questions")
    public Result<?> addQuestionToExam(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        assertExamOwner(examId);
        ExamQuestion eq = examQuestionService.addQuestionToExam(examId, body);
        return Result.success(Map.of("id", eq.getId()));
    }

    //更新题目在考试中的分数
    @PutMapping("/{examId}/questions/score")
    public Result<?> updateQuestionScore(@PathVariable Long examId, @RequestBody Map<String, Object> body) {
        assertExamOwner(examId);
        examQuestionService.updateQuestionScore(examId, body);
        return Result.success();
    }

    //从考试中移除题目
    @DeleteMapping("/{examId}/questions/{questionId}")
    public Result<?> removeQuestionFromExam(@PathVariable Long examId, @PathVariable Long questionId) {
        assertExamOwner(examId);
        boolean removed = examQuestionService.removeQuestionFromExam(examId, questionId);
        return Result.success(removed);
    }

    //获取考试统计信息
    @GetMapping("/{examId}/stats")
    public Result<ExamStatsVO> getExamStats(@PathVariable Long examId) {
        assertExamOwner(examId);
        return Result.success(examRecordService.getExamStats(examId));
    }

    /**
     * 按JWT身份校验试卷归属：超级管理员放行，教师只能操作自己创建的考试
     */
    private void assertExamOwner(Long examId) {
        if (SecurityUtils.isSuperAdmin()) {
            return;
        }
        Exam exam = examService.getById(examId);
        Long currentUserId = SecurityUtils.getUserId();
        if (exam == null || !Objects.equals(exam.getCreatorId(), currentUserId)) {
            throw new AccessDeniedException("只能操作自己创建的考试");
        }
    }

    /**
     * 按JWT身份校验试卷可见性：超级管理员放行；
     * 教师看自己创建的，学生只能看其所属用户组已绑定的试卷（未绑定用户组的试卷视为公开）
     */
    private void assertExamAccessible(Long examId) {
        Long currentUserId = SecurityUtils.getUserId();
        Integer role = SecurityUtils.getRole();
        if (currentUserId == null || role == null) {
            throw new AccessDeniedException("未登录或登录已过期");
        }
        if (!examService.canAccess(examId, currentUserId, role)) {
            throw new AccessDeniedException("无权访问该试卷");
        }
    }
}

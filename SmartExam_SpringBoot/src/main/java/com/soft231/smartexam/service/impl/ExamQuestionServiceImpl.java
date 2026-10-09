package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.ExamQuestion;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.mapper.ExamQuestionMapper;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

//考试题目关联服务实现类
//题目关联：获取题目列表 getExamQuestions / 添加题目 addQuestionToExam / 更新分数 updateQuestionScore / 移除题目 removeQuestionFromExam
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements ExamQuestionService {

    @Autowired
    private QuestionService questionService;

    //获取考试的题目列表（includeAnswer=false 时不下发答案与解析，供学生作答使用）
    @Override
    public List<Map<String, Object>> getExamQuestions(Long examId, boolean includeAnswer) {
        List<ExamQuestion> examQuestions = this.lambdaQuery()
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSortOrder)
                .list();

        if (examQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        // 一次性批量取出所有题目，避免循环内逐条 getById 产生的 N+1 查询
        List<Long> questionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question question = questionMap.get(eq.getQuestionId());
            if (question == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", question.getId());
            item.put("type", question.getType());
            item.put("content", question.getContent());
            item.put("options", question.getOptions());
            // 答案与解析只在教师/超管编辑试卷时下发，学生作答接口不返回，避免提前泄题
            if (includeAnswer) {
                item.put("answer", question.getAnswer());
                item.put("analysis", question.getAnalysis());
            }
            item.put("score", eq.getScore());
            item.put("examScore", eq.getScore());
            item.put("examQuestionId", eq.getId());
            item.put("sortOrder", eq.getSortOrder() == null ? 0 : eq.getSortOrder());
            result.add(item);
        }

        // 尊重教师在试卷中设置的题目顺序：先按序号，序号相同再按题型归组，
        // 最后用题目 id 兜底 —— 保证排序结果完全确定，不会因 MySQL 返回顺序不稳定而前后两次刷新看到不同题序
        result.sort(Comparator
                .comparingInt((Map<String, Object> a) -> (Integer) a.get("sortOrder"))
                .thenComparingInt(a -> (Integer) a.get("type"))
                .thenComparingLong(a -> ((Number) a.get("id")).longValue()));
        return result;
    }

    //向考试添加题目
    //sort_order（题目在试卷中的展示序号）由服务端按「当前最大序号 + 1」计算：
    //前端原先用「已选题数量」当序号，删除中间某题后序号会出现空洞，再次加题就会与已有题撞号，
    //撞号时排序退化为按题型兜底，题目顺序不再稳定。改由服务端取 max+1 可彻底避免。
    @Override
    public ExamQuestion addQuestionToExam(Long examId, Map<String, Object> body) {
        Long questionId = ((Number) body.get("questionId")).longValue();
        Integer score = body.get("score") != null ? ((Number) body.get("score")).intValue() : 10;

        int nextSort = this.lambdaQuery()
                .eq(ExamQuestion::getExamId, examId)
                .list()
                .stream()
                .mapToInt(eq -> eq.getSortOrder() == null ? 0 : eq.getSortOrder())
                .max()
                .orElse(-1) + 1;

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamId(examId);
        examQuestion.setQuestionId(questionId);
        examQuestion.setScore(score);
        examQuestion.setSortOrder(nextSort);

        this.save(examQuestion);
        return examQuestion;
    }

    //更新题目在考试中的分数
    @Override
    public void updateQuestionScore(Long examId, Map<String, Object> body) {
        Long examQuestionId = ((Number) body.get("examQuestionId")).longValue();
        Integer score = ((Number) body.get("score")).intValue();

        ExamQuestion examQuestion = this.getById(examQuestionId);
        if (examQuestion != null && examQuestion.getExamId().equals(examId)) {
            examQuestion.setScore(score);
            this.updateById(examQuestion);
        }
    }

    //从考试中移除题目
    //移除后对剩余题目重新编号（0,1,2...连续），消除序号空洞，保证"第几题"与题号始终一一对应
    @Override
    @Transactional
    public boolean removeQuestionFromExam(Long examId, Long questionId) {
        boolean removed = this.lambdaUpdate()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId)
                .remove();

        if (removed) {
            List<ExamQuestion> rest = this.lambdaQuery()
                    .eq(ExamQuestion::getExamId, examId)
                    .orderByAsc(ExamQuestion::getSortOrder)
                    .orderByAsc(ExamQuestion::getId)
                    .list();
            for (int i = 0; i < rest.size(); i++) {
                ExamQuestion eq = rest.get(i);
                if (!Objects.equals(eq.getSortOrder(), i)) {
                    eq.setSortOrder(i);
                    this.updateById(eq);
                }
            }
        }
        return removed;
    }
}
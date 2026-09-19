package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.mapper.ExamMapper;
import com.soft231.smartexam.mapper.ExamRecordMapper;
import com.soft231.smartexam.service.AnswerRecordService;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.ExamRecordService;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamStatsVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//考试记录服务实现类
//考试流程：开始考试 startExam / 提交考试 submitExam / 创建记录 createRecord
//成绩查询：用户成绩列表 getRecordsWithExamInfoByUserId / 记录详情 getRecordById / 考试所有记录 getRecordsByExamId / 考试状态 getExamRecordStatus
//批阅打分：获取答题详情 getRecordAnswers / 自动批改客观题 autoGradeObjectiveQuestions / 提交分数 submitScores / 获取所有记录 getAllRecordsWithInfo
//记录管理：更新总分 updateTotalScore / 更新状态 updateRecordStatus / 已提交ID列表 getSubmittedExamIdsByUserId
//统计信息：考试提交统计 getExamSubmissionStats / 考试统计 getExamStats
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Autowired
    private AnswerRecordService answerRecordService;

    @Autowired
    private ExamQuestionService examQuestionService;
    
    @Autowired
    private ExamMapper examMapper;

    //获取考试记录的答案列表
    @Override
    public List<Map<String, Object>> getRecordAnswers(Long recordId) {
        List<AnswerRecordVO> answers = answerRecordService.getAnswersWithQuestion(recordId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AnswerRecordVO vo : answers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", vo.getId());
            item.put("examRecordId", vo.getExamRecordId());
            item.put("questionId", vo.getQuestionId());
            item.put("userAnswer", vo.getUserAnswer());
            item.put("score", vo.getScore());
            item.put("comment", vo.getComment());
            item.put("content", vo.getContent());
            item.put("type", vo.getType());
            item.put("options", vo.getOptions());
            item.put("answer", vo.getAnswer());
            item.put("analysis", vo.getAnalysis());
            item.put("examScore", vo.getExamScore() != null ? vo.getExamScore() : 10);
            result.add(item);
        }
        return result;
    }

    //获取用户的考试记录列表
    @Override
    public List<ExamRecordVO> getRecordsWithExamInfoByUserId(Long userId) {
        return baseMapper.selectRecordsWithInfoByUserId(userId);
    }

    //根据ID获取考试记录详情
    @Override
    public ExamRecordVO getRecordById(Long id) {
        return baseMapper.selectRecordWithInfoById(id);
    }

    //获取用户已提交的考试ID列表
    @Override
    public List<Long> getSubmittedExamIdsByUserId(Long userId) {
        List<ExamRecord> records = baseMapper.selectByUserId(userId);

        return records.stream()
                .filter(record -> record.getStatus() == 2)
                .map(ExamRecord::getExamId)
                .distinct()
                .collect(Collectors.toList());
    }

    //更新考试记录的总分
    @Override
    public boolean updateTotalScore(Long recordId, Integer totalScore) {
        ExamRecord record = new ExamRecord();
        record.setId(recordId);
        record.setScore(totalScore);
        return this.updateById(record);
    }

    //更新考试记录状态
    @Override
    public boolean updateRecordStatus(Long recordId, Integer status) {
        ExamRecord record = new ExamRecord();
        record.setId(recordId);
        record.setStatus(status);
        return this.updateById(record);
    }

    //创建考试记录
    @Override
    public ExamRecord createRecord(ExamRecord record) {
        record.setStatus(1);
        record.setSubmitTime(java.time.LocalDateTime.now());
        this.save(record);
        return record;
    }

    //提交考试
    @Override
    @org.springframework.transaction.annotation.Transactional
    public ExamRecord submitExam(Long userId, Long examId, String answersJson) {
        ExamRecord record = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        if (record == null) {
            record = new ExamRecord();
            record.setUserId(userId);
            record.setExamId(examId);
            record.setStartTime(java.time.LocalDateTime.now());
        }
        
        record.setSubmitTime(java.time.LocalDateTime.now());
        record.setStatus(2);
        this.saveOrUpdate(record);

        if (answersJson != null && !answersJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.Map<String, Object> answersMap = mapper.readValue(answersJson, java.util.Map.class);
                
                java.util.List<com.soft231.smartexam.entity.ExamQuestion> examQuestions = 
                    examQuestionService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.soft231.smartexam.entity.ExamQuestion>()
                        .eq(com.soft231.smartexam.entity.ExamQuestion::getExamId, examId)
                        .orderByAsc(com.soft231.smartexam.entity.ExamQuestion::getSortOrder));

                for (com.soft231.smartexam.entity.ExamQuestion eq : examQuestions) {
                    Object userAnswer = answersMap.get(String.valueOf(eq.getQuestionId()));
                    
                    if (userAnswer != null) {
                        String answerStr;
                        if (userAnswer instanceof java.util.List) {
                            answerStr = mapper.writeValueAsString(userAnswer);
                        } else if (userAnswer instanceof Number) {
                            answerStr = String.valueOf(userAnswer);
                        } else {
                            answerStr = String.valueOf(userAnswer);
                        }
                        
                        com.soft231.smartexam.entity.AnswerRecord ar = new com.soft231.smartexam.entity.AnswerRecord();
                        ar.setExamRecordId(record.getId());
                        ar.setQuestionId(eq.getQuestionId());
                        ar.setUserAnswer(answerStr);
                        answerRecordService.save(ar);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return record;
    }

    //提交分数
    @Override
    @Transactional
    public void submitScores(Long recordId, List<Map<String, Object>> scoreList) {
        answerRecordService.batchUpdateScores(scoreList);

        int totalScore = scoreList.stream()
                .mapToInt(item -> ((Number) item.get("score")).intValue())
                .sum();
        this.updateTotalScore(recordId, totalScore);
        this.updateRecordStatus(recordId, 2);
    }

    //获取考试提交统计信息
    @Override
    public List<ExamSubmissionStatsVO> getExamSubmissionStats(Long creatorId) {
        if (creatorId != null) {
            return baseMapper.selectExamSubmissionStatsByCreatorId(creatorId);
        } else {
            return baseMapper.selectAllExamSubmissionStats();
        }
    }

    //获取考试的所有记录
    @Override
    public List<ExamRecordVO> getRecordsByExamId(Long examId) {
        return baseMapper.selectRecordsWithUserByExamId(examId);
    }

    //获取考试统计信息
    @Override
    public ExamStatsVO getExamStats(Long examId) {
        return baseMapper.selectExamStatsById(examId);
    }

    //获取用户的考试状态
    @Override
    public Map<String, Object> getExamRecordStatus(Long userId, Long examId) {
        ExamRecord record = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        Map<String, Object> result = new HashMap<>();
        if (record != null) {
            result.put("hasRecord", true);
            result.put("recordId", record.getId());
            result.put("startTime", record.getStartTime());
            result.put("status", record.getStatus());
        } else {
            result.put("hasRecord", false);
            result.put("recordId", null);
            result.put("startTime", null);
            result.put("status", null);
        }
        return result;
    }

    //开始考试
    @Override
    public ExamRecord startExam(Long userId, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam != null && exam.getEndTime() != null) {
            if (java.time.LocalDateTime.now().isAfter(exam.getEndTime())) {
                throw new RuntimeException("考试已结束，不可再进入");
            }
        }
        
        ExamRecord existingRecord = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        if (existingRecord != null) {
            if (existingRecord.getStatus() == 2) {
                throw new RuntimeException("该考试已提交，不可再进入");
            }
            return existingRecord;
        }
        
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStartTime(java.time.LocalDateTime.now());
        record.setStatus(1);
        this.save(record);
        return record;
    }

    //自动批改客观题
    @Override
    public List<Map<String, Object>> autoGradeObjectiveQuestions(Long recordId) {
        List<AnswerRecordVO> answers = answerRecordService.getAnswersWithQuestion(recordId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AnswerRecordVO answer : answers) {
            Map<String, Object> item = new HashMap<>();
            Integer type = answer.getType();
            Long answerId = answer.getId();
            String userAnswer = answer.getUserAnswer();
            String correctAnswer = answer.getAnswer();
            Integer score = answer.getExamScore();
            Integer currentScore = answer.getScore();

            item.put("id", answerId);

            if (type != null && (type == 0 || type == 1 || type == 2 || type == 3)) {
                boolean isCorrect = false;
                Integer finalScore = 0;
                if (userAnswer != null && correctAnswer != null && !userAnswer.isEmpty() && !correctAnswer.isEmpty()) {
                    if (type == 1) {
                        String[] userArr = userAnswer.split(",");
                        String[] correctArr = correctAnswer.split(",");
                        java.util.Set<String> userSet = new java.util.HashSet<>();
                        java.util.Set<String> correctSet = new java.util.HashSet<>();
                        for(String s : userArr){
                            String trim = s.trim();
                            if(!trim.isEmpty()) userSet.add(trim);
                        }
                        for(String s : correctArr){
                            String trim = s.trim();
                            if(!trim.isEmpty()) correctSet.add(trim);
                        }

                        boolean hasWrongAnswer = false;
                        for (String userOpt : userSet) {
                            if (!correctSet.contains(userOpt)) {
                                hasWrongAnswer = true;
                                break;
                            }
                        }

                        if (!hasWrongAnswer) {
                            // 无错误选项
                            boolean allMatch = true;
                            for(String c : correctSet){
                                if(!userSet.contains(c)){
                                    allMatch = false;
                                    break;
                                }
                            }
                            if(allMatch){
                                // 全对，满分
                                finalScore = score;
                                isCorrect = true;
                            }else{
                                // 少选部分正确，给一半分
                                finalScore = score / 2;
                                isCorrect = false;
                            }
                        }
                        // 有错选：finalScore保持0
                    } else if (type == 3) {
                        String[] userArr = userAnswer.split(",");
                        String[] correctArr = correctAnswer.split(",");
                        if (userArr.length == correctArr.length) {
                            java.util.Arrays.sort(userArr);
                            java.util.Arrays.sort(correctArr);
                            isCorrect = String.join(",", userArr).equals(String.join(",", correctArr));
                            if (isCorrect) {
                                finalScore = score;
                            }
                        }
                    } else {
                        isCorrect = userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
                        if (isCorrect) {
                            finalScore = score;
                        }
                    }
                }

                item.put("score", finalScore);
                item.put("isObjective", true);
                item.put("isCorrect", isCorrect);
            } else {
                item.put("score", currentScore != null ? currentScore : 0);
                item.put("isObjective", false);
                item.put("isCorrect", false);
            }

            item.put("comment", answer.getComment());
            result.add(item);
        }

        return result;
    }

    //获取所有考试记录
    @Override
    public List<ExamRecordVO> getAllRecordsWithInfo() {
        List<ExamRecord> records = this.list();
        
        return records.stream()
                .map(record -> baseMapper.selectRecordWithInfoById(record.getId()))
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }
}
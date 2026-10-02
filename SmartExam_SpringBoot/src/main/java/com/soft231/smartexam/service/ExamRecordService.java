package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamStatsVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;

import java.util.List;
import java.util.Map;

//考试记录服务接口
//获取考试记录的答案列表
//根据ID获取考试记录详情
//获取用户的考试记录列表
//获取用户已提交的考试ID列表
//更新考试记录的总分
//更新考试记录状态
//创建考试记录
//提交考试
//保存答题草稿
//兜底自动交卷
//提交分数
//获取考试提交统计信息
//获取考试的所有记录
//获取考试统计信息
//获取用户的考试状态
//开始考试
//自动批改客观题
//获取所有考试记录
public interface ExamRecordService extends IService<ExamRecord> {

    //获取考试记录的答案列表
    List<Map<String, Object>> getRecordAnswers(Long recordId);

    //根据ID获取考试记录详情（关联用户和考试信息）
    ExamRecordVO getRecordById(Long id);

    //获取用户的考试记录列表（数据库分页）
    IPage<ExamRecordVO> getRecordsWithExamInfoByUserId(IPage<ExamRecordVO> page, Long userId);

    //获取用户已提交的考试ID列表
    List<Long> getSubmittedExamIdsByUserId(Long userId);

    //更新考试记录的总分
    boolean updateTotalScore(Long recordId, Integer totalScore);

    //更新考试记录状态
    boolean updateRecordStatus(Long recordId, Integer status);

    //创建考试记录
    ExamRecord createRecord(ExamRecord record);

    //提交考试
    ExamRecord submitExam(Long userId, Long examId, String answersJson);

    //保存答题草稿（自动存卷，不改变考试记录状态）
    void saveDraft(Long userId, Long examId, String answersJson);

    //兜底自动交卷：把已超过作答截止时间但仍为"进行中"的记录强制交卷，返回处理条数
    int autoSubmitExpiredRecords();

    //提交分数
    void submitScores(Long recordId, List<Map<String, Object>> scoreList);

    //获取考试提交统计信息（数据库分页）
    IPage<ExamSubmissionStatsVO> getExamSubmissionStats(IPage<ExamSubmissionStatsVO> page, Long creatorId);

    //获取考试的所有记录（数据库分页）
    IPage<ExamRecordVO> getRecordsByExamId(IPage<ExamRecordVO> page, Long examId);

    //获取考试统计信息（多表JOIN查询）
    ExamStatsVO getExamStats(Long examId);

    //获取用户的考试状态
    //withAnswers=true 时额外回带已保存的答案（页面加载用），心跳轮询传 false 以免反复传输长文本
    Map<String, Object> getExamRecordStatus(Long userId, Long examId, boolean withAnswers);

    //开始考试
    ExamRecord startExam(Long userId, Long examId);

    //自动批改客观题
    List<Map<String, Object>> autoGradeObjectiveQuestions(Long recordId);

    //获取所有考试记录（数据库分页）
    IPage<ExamRecordVO> getAllRecordsWithInfo(IPage<ExamRecordVO> page);
}
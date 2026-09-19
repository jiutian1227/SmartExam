package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;

import java.util.List;
import java.util.Map;

//答案记录服务接口
//获取答案记录及其题目信息
//批量保存答案
//更新答案分数
//批量更新分数
public interface AnswerRecordService extends IService<AnswerRecord> {

    //获取答案记录及其题目信息（多表JOIN查询）
    List<AnswerRecordVO> getAnswersWithQuestion(Long examRecordId);

    //批量保存答案
    boolean batchSaveAnswers(List<AnswerRecord> answers);

    //更新答案分数
    boolean updateScore(Long answerId, Integer score, String comment);

    //批量更新分数
    boolean batchUpdateScores(List<Map<String, Object>> scoreList);
}
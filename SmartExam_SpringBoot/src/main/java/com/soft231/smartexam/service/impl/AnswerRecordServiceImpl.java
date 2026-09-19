package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.mapper.AnswerRecordMapper;
import com.soft231.smartexam.service.AnswerRecordService;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

//答案记录服务实现类
//查询：获取答案记录及题目信息 getAnswersWithQuestion
//批阅：批量保存答案 batchSaveAnswers / 更新答案分数 updateScore / 批量更新分数 batchUpdateScores
@Service
public class AnswerRecordServiceImpl extends ServiceImpl<AnswerRecordMapper, AnswerRecord> implements AnswerRecordService {

    //获取答案记录及其题目信息
    @Override
    public List<AnswerRecordVO> getAnswersWithQuestion(Long examRecordId) {
        return baseMapper.selectAnswersWithQuestion(examRecordId);
    }

    //批量保存答案
    @Override
    @Transactional
    public boolean batchSaveAnswers(List<AnswerRecord> answers) {
        return this.saveBatch(answers);
    }

    //更新答案分数
    @Override
    public boolean updateScore(Long answerId, Integer score, String comment) {
        AnswerRecord answer = new AnswerRecord();
        answer.setId(answerId);
        answer.setScore(score);
        answer.setComment(comment);
        return this.updateById(answer);
    }

    //批量更新分数
    @Override
    @Transactional
    public boolean batchUpdateScores(List<Map<String, Object>> scoreList) {
        for (Map<String, Object> item : scoreList) {
            Long answerId = ((Number) item.get("answerRecordId")).longValue();
            Integer score = ((Number) item.get("score")).intValue();
            String comment = (String) item.get("comment");

            this.updateScore(answerId, score, comment);
        }
        return true;
    }
}
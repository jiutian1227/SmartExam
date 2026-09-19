package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.AnswerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class AnswerRecordMapperTest {

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Test
    public void insertTest() {
        AnswerRecord record = new AnswerRecord();
        record.setExamRecordId(1L);
        record.setQuestionId(1L);
        record.setUserAnswer("A");
        record.setScore(10);
        record.setComment("回答正确");
        int result = answerRecordMapper.insert(record);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + record.getId());
    }

    @Test
    public void updateTest() {
        AnswerRecord record = new AnswerRecord();
        record.setExamRecordId(1L);
        record.setQuestionId(1L);
        record.setUserAnswer("B");
        record.setScore(0);
        answerRecordMapper.insert(record);
        System.out.println("插入ID: " + record.getId());

        AnswerRecord updateRecord = new AnswerRecord();
        updateRecord.setId(record.getId());
        updateRecord.setUserAnswer("A");
        updateRecord.setScore(10);
        updateRecord.setComment("已更正，回答正确");
        int result = answerRecordMapper.updateById(updateRecord);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        AnswerRecord record = new AnswerRecord();
        record.setExamRecordId(1L);
        record.setQuestionId(1L);
        record.setUserAnswer("C");
        answerRecordMapper.insert(record);
        System.out.println("待删除ID: " + record.getId());

        int result = answerRecordMapper.deleteById(record.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        AnswerRecord record = answerRecordMapper.selectById(1L);
        System.out.println(record);
    }

    @Test
    public void selectAllTest() {
        List<AnswerRecord> list = answerRecordMapper.selectList(null);
        for (AnswerRecord r : list) {
            System.out.println(r);
        }
    }


    @Test
    public void selectAnswersWithQuestionTest() {
        List<?> list = answerRecordMapper.selectAnswersWithQuestion(1L);
        System.out.println("答题记录(含题目信息):");
        for (Object o : list) {
            System.out.println(o);
        }
    }
}

package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.ExamRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class ExamRecordMapperTest {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Test
    public void insertTest() {
        ExamRecord record = new ExamRecord();
        record.setExamId(1L);
        record.setUserId(1L);
        record.setStartTime(LocalDateTime.now());
        record.setSubmitTime(LocalDateTime.now().plusMinutes(30));
        record.setScore(85);
        record.setStatus(1);
        int result = examRecordMapper.insert(record);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + record.getId());
    }

    @Test
    public void updateTest() {
        ExamRecord record = new ExamRecord();
        record.setExamId(1L);
        record.setUserId(1L);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        examRecordMapper.insert(record);
        System.out.println("插入ID: " + record.getId());

        ExamRecord updateRecord = new ExamRecord();
        updateRecord.setId(record.getId());
        updateRecord.setSubmitTime(LocalDateTime.now());
        updateRecord.setScore(90);
        updateRecord.setStatus(1);
        int result = examRecordMapper.updateById(updateRecord);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        ExamRecord record = new ExamRecord();
        record.setExamId(1L);
        record.setUserId(1L);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        examRecordMapper.insert(record);
        System.out.println("待删除ID: " + record.getId());

        int result = examRecordMapper.deleteById(record.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        ExamRecord record = examRecordMapper.selectById(1L);
        System.out.println(record);
    }

    @Test
    public void selectAllTest() {
        List<ExamRecord> list = examRecordMapper.selectList(null);
        for (ExamRecord r : list) {
            System.out.println(r);
        }
    }


    @Test
    public void selectByUserIdTest() {
        List<ExamRecord> list = examRecordMapper.selectByUserId(1L);
        System.out.println("用户1的考试记录:");
        for (ExamRecord r : list) {
            System.out.println(r);
        }
    }

    @Test
    public void selectRecordsWithInfoByUserIdTest() {
        List<?> list = examRecordMapper.selectRecordsWithInfoByUserId(1L);
        System.out.println("用户1的考试记录(含信息):");
        for (Object o : list) {
            System.out.println(o);
        }
    }

    @Test
    public void selectRecordWithInfoByIdTest() {
        Object vo = examRecordMapper.selectRecordWithInfoById(1L);
        System.out.println("记录详情: " + vo);
    }

    @Test
    public void selectRecordsWithUserByExamIdTest() {
        List<?> list = examRecordMapper.selectRecordsWithUserByExamId(1L);
        System.out.println("考试1的所有记录(含用户):");
        for (Object o : list) {
            System.out.println(o);
        }
    }

    @Test
    public void selectExamStatsByIdTest() {
        Object stats = examRecordMapper.selectExamStatsById(1L);
        System.out.println("考试1统计数据: " + stats);
    }

    @Test
    public void selectExamSubmissionStatsByCreatorIdTest() {
        List<?> list = examRecordMapper.selectExamSubmissionStatsByCreatorId(1L);
        System.out.println("创建者1的考试提交统计:");
        for (Object o : list) {
            System.out.println(o);
        }
    }

    @Test
    public void selectAllExamSubmissionStatsTest() {
        List<?> list = examRecordMapper.selectAllExamSubmissionStats();
        System.out.println("所有考试提交统计:");
        for (Object o : list) {
            System.out.println(o);
        }
    }
}

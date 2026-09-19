package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soft231.smartexam.entity.Exam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class ExamMapperTest {

    @Autowired
    private ExamMapper examMapper;

    @Test
    public void insertTest() {
        Exam exam = new Exam();
        exam.setTitle("期中考试");
        exam.setDescription("Java基础期中测试");
        exam.setUserGroupIds("1,2");
        exam.setStartTime(LocalDateTime.now());
        exam.setEndTime(LocalDateTime.now().plusHours(2));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setCreatorId(1L);
        int result = examMapper.insert(exam);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + exam.getId());
    }

    @Test
    public void updateTest() {
        Exam exam = new Exam();
        exam.setTitle("待更新考试");
        exam.setStartTime(LocalDateTime.now());
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        exam.setDuration(60);
        exam.setTotalScore(50);
        exam.setCreatorId(1L);
        examMapper.insert(exam);
        System.out.println("插入ID: " + exam.getId());

        Exam updateExam = new Exam();
        updateExam.setId(exam.getId());
        updateExam.setTitle("已更新考试标题");
        updateExam.setDescription("这是更新后的描述");
        int result = examMapper.updateById(updateExam);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        Exam exam = new Exam();
        exam.setTitle("待删除考试");
        exam.setStartTime(LocalDateTime.now());
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        exam.setDuration(60);
        exam.setTotalScore(50);
        exam.setCreatorId(1L);
        examMapper.insert(exam);
        System.out.println("待删除考试ID: " + exam.getId());

        int result = examMapper.deleteById(exam.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        Exam exam = examMapper.selectById(1L);
        System.out.println(exam);
    }

    @Test
    public void selectAllTest() {
        List<Exam> exams = examMapper.selectList(null);
        for (Exam e : exams) {
            System.out.println(e);
        }
    }


    @Test
    public void listForUserTest() {
        IPage<Exam> page = new Page<>(1, 10);
        IPage<Exam> result = examMapper.listForUser(page, 1L);
        System.out.println("用户1可参加的考试(分页):");
        System.out.println("总记录数: " + result.getTotal());
        System.out.println("当前页: " + result.getCurrent());
        System.out.println("每页条数: " + result.getSize());
        for (Exam e : result.getRecords()) {
            System.out.println(e);
        }
    }
}

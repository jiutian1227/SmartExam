package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.ExamQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class ExamQuestionMapperTest {

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Test
    public void insertTest() {
        ExamQuestion eq = new ExamQuestion();
        eq.setExamId(1L);
        eq.setQuestionId(1L);
        eq.setScore(10);
        eq.setSortOrder(1);
        int result = examQuestionMapper.insert(eq);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + eq.getId());
    }

    @Test
    public void updateTest() {
        ExamQuestion eq = new ExamQuestion();
        eq.setExamId(1L);
        eq.setQuestionId(2L);
        eq.setScore(5);
        eq.setSortOrder(1);
        examQuestionMapper.insert(eq);
        System.out.println("插入ID: " + eq.getId());

        ExamQuestion updateEq = new ExamQuestion();
        updateEq.setId(eq.getId());
        updateEq.setScore(20);
        updateEq.setSortOrder(99);
        int result = examQuestionMapper.updateById(updateEq);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        ExamQuestion eq = new ExamQuestion();
        eq.setExamId(1L);
        eq.setQuestionId(3L);
        eq.setScore(10);
        eq.setSortOrder(1);
        examQuestionMapper.insert(eq);
        System.out.println("待删除ID: " + eq.getId());

        int result = examQuestionMapper.deleteById(eq.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        ExamQuestion eq = examQuestionMapper.selectById(1L);
        System.out.println(eq);
    }

    @Test
    public void selectAllTest() {
        List<ExamQuestion> list = examQuestionMapper.selectList(null);
        for (ExamQuestion eq : list) {
            System.out.println(eq);
        }
    }
}

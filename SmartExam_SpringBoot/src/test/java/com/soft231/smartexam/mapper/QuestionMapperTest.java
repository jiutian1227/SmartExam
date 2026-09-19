package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class QuestionMapperTest {

    @Autowired
    private QuestionMapper questionMapper;

    @Test
    public void insertTest() {
        Question question = new Question();
        question.setType(1);
        question.setContent("以下哪个是Java关键字？");
        question.setOptions("[\"A. int\", \"B. integer\", \"C. String\", \"D. Array\"]");
        question.setAnswer("A");
        question.setAnalysis("int是Java的基本数据类型关键字");
        question.setKnowledgePointId(1L);
        question.setCreatorId(1L);
        int result = questionMapper.insert(question);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + question.getId());
    }

    @Test
    public void updateTest() {
        Question question = new Question();
        question.setType(2);
        question.setContent("判断题测试题");
        question.setAnswer("对");
        question.setCreatorId(1L);
        questionMapper.insert(question);
        System.out.println("插入ID: " + question.getId());

        Question updateQuest = new Question();
        updateQuest.setId(question.getId());
        updateQuest.setContent("判断题测试题（已更新）");
        updateQuest.setAnalysis("这是更新后的解析");
        int result = questionMapper.updateById(updateQuest);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        Question question = new Question();
        question.setType(3);
        question.setContent("填空题: Java的创始人之一是___");
        question.setAnswer("James Gosling");
        question.setCreatorId(1L);
        questionMapper.insert(question);
        System.out.println("待删除题目ID: " + question.getId());

        int result = questionMapper.deleteById(question.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        Question question = questionMapper.selectById(1L);
        System.out.println(question);
    }

    @Test
    public void selectAllTest() {
        List<Question> questions = questionMapper.selectList(null);
        for (Question q : questions) {
            System.out.println(q);
        }
    }
}

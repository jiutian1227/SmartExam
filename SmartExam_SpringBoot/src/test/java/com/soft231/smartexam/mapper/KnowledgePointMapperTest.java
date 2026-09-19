package com.soft231.smartexam.mapper;

import com.soft231.smartexam.entity.KnowledgePoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class KnowledgePointMapperTest {

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Test
    public void insertTest() {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setName("Java基础");
        kp.setCreatorId(1L);
        int result = knowledgePointMapper.insert(kp);
        System.out.println("插入结果: " + result);
        System.out.println("生成ID: " + kp.getId());
    }

    @Test
    public void updateTest() {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setName("待更新知识点");
        kp.setCreatorId(1L);
        knowledgePointMapper.insert(kp);
        System.out.println("插入ID: " + kp.getId());

        KnowledgePoint updateKp = new KnowledgePoint();
        updateKp.setId(kp.getId());
        updateKp.setName("已更新的知识点名称");
        int result = knowledgePointMapper.updateById(updateKp);
        System.out.println("更新结果: " + result);
    }

    @Test
    public void deleteTest() {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setName("待删除知识点");
        kp.setCreatorId(1L);
        knowledgePointMapper.insert(kp);
        System.out.println("待删除知识点ID: " + kp.getId());

        int result = knowledgePointMapper.deleteById(kp.getId());
        System.out.println("删除结果: " + result);
    }

    @Test
    public void selectByIdTest() {
        KnowledgePoint kp = knowledgePointMapper.selectById(1L);
        System.out.println(kp);
    }

    @Test
    public void selectAllTest() {
        List<KnowledgePoint> list = knowledgePointMapper.selectList(null);
        for (KnowledgePoint kp : list) {
            System.out.println(kp);
        }
    }
}

package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.KnowledgePoint;

import java.util.List;

//知识点服务接口
//根据创建者ID获取知识点列表
//获取所有知识点列表
public interface KnowledgePointService extends IService<KnowledgePoint> {

    //根据创建者ID获取知识点列表
    List<KnowledgePoint> getKnowledgePointsByCreatorId(Long creatorId);

    //获取所有知识点列表
    List<KnowledgePoint> getAllKnowledgePoints();
}
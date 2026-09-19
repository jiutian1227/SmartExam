package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.mapper.KnowledgePointMapper;
import com.soft231.smartexam.service.KnowledgePointService;
import org.springframework.stereotype.Service;

import java.util.List;

//知识点服务实现类
//查询功能：按创建者查询 getKnowledgePointsByCreatorId / 查询全部 getAllKnowledgePoints
@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> implements KnowledgePointService {

    //根据创建者ID获取知识点列表
    @Override
    public List<KnowledgePoint> getKnowledgePointsByCreatorId(Long creatorId) {
        return this.list(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(creatorId != null, KnowledgePoint::getCreatorId, creatorId)
                .orderByDesc(KnowledgePoint::getCreateTime));
    }

    //获取所有知识点列表
    @Override
    public List<KnowledgePoint> getAllKnowledgePoints() {
        return this.list(new LambdaQueryWrapper<KnowledgePoint>()
                .orderByDesc(KnowledgePoint::getCreateTime));
    }
}
package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//知识点管理控制器
//知识点CRUD：创建 create / 查询 getById / 更新 update / 删除 delete
//查询功能：按创建者查询 getByCreatorId / 查询全部 getAll
@RestController
@RequestMapping("/api/knowledge-point")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    //创建知识点
    @PostMapping
    public Result<KnowledgePoint> create(@RequestBody KnowledgePoint knowledgePoint) {
        knowledgePointService.save(knowledgePoint);
        return Result.success(knowledgePoint);
    }

    //根据ID查询知识点
    @GetMapping("/{id}")
    public Result<KnowledgePoint> getById(@PathVariable Long id) {
        return Result.success(knowledgePointService.getById(id));
    }

    //更新知识点信息
    @PutMapping
    public Result<KnowledgePoint> update(@RequestBody KnowledgePoint knowledgePoint) {
        knowledgePointService.updateById(knowledgePoint);
        return Result.success(knowledgePoint);
    }

    //删除知识点
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgePointService.removeById(id);
        return Result.success();
    }

    //根据创建者ID获取知识点列表
    @GetMapping("/creator/{creatorId}")
    public Result<List<KnowledgePoint>> getByCreatorId(@PathVariable Long creatorId) {
        return Result.success(knowledgePointService.getKnowledgePointsByCreatorId(creatorId));
    }

    //获取所有知识点列表
    @GetMapping("/all")
    public Result<List<KnowledgePoint>> getAll() {
        return Result.success(knowledgePointService.getAllKnowledgePoints());
    }
}

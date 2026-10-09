package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.service.KnowledgePointService;
import com.soft231.smartexam.service.QuestionService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

//知识点管理控制器
//知识点CRUD：创建 create / 查询 getById / 更新 update / 删除 delete
//查询功能：当前用户的知识点 getMy / 查询全部 getAll
@RestController
@RequestMapping("/api/knowledge-point")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private QuestionService questionService;

    //创建知识点 —— 教师及以上（creatorId以JWT身份为准，不信任前端传参）
    @PostMapping
    public Result<KnowledgePoint> create(@RequestBody KnowledgePoint knowledgePoint) {
        assertTeacherOrAbove();
        knowledgePoint.setCreatorId(SecurityUtils.getUserId());
        knowledgePointService.save(knowledgePoint);
        return Result.success(knowledgePoint);
    }

    //根据ID查询知识点 —— 教学资源管理，仅教师及以上（SecurityConfig已限角色，此处纵深防御）
    @GetMapping("/{id}")
    public Result<KnowledgePoint> getById(@PathVariable Long id) {
        assertTeacherOrAbove();
        return Result.success(knowledgePointService.getById(id));
    }

    //更新知识点信息 —— 教师及以上；教师只能改自己创建的知识点，超管不限
    @PutMapping
    public Result<KnowledgePoint> update(@RequestBody KnowledgePoint knowledgePoint) {
        assertTeacherOrAbove();
        if (!SecurityUtils.isSuperAdmin()) {
            KnowledgePoint existing = knowledgePointService.getById(knowledgePoint.getId());
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能修改自己创建的知识点");
            }
            knowledgePoint.setCreatorId(existing.getCreatorId());
        }
        knowledgePointService.updateById(knowledgePoint);
        return Result.success(knowledgePoint);
    }

    //删除知识点 —— 教师及以上；教师只能删自己创建的知识点，超管不限
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assertTeacherOrAbove();
        if (!SecurityUtils.isSuperAdmin()) {
            KnowledgePoint existing = knowledgePointService.getById(id);
            if (existing == null || !Objects.equals(existing.getCreatorId(), SecurityUtils.getUserId())) {
                throw new AccessDeniedException("只能删除自己创建的知识点");
            }
        }
        // question.knowledge_point_id 外键是 ON DELETE RESTRICT，有题目引用时硬删会被数据库拒绝并抛500，
        // 这里提前拦下给出可读提示
        Long questionCount = questionService.lambdaQuery()
                .eq(Question::getKnowledgePointId, id)
                .count();
        if (questionCount != null && questionCount > 0) {
            return Result.error(400, "该知识点下还有 " + questionCount + " 道题目，请先转移或删除这些题目");
        }
        knowledgePointService.removeById(id);
        return Result.success();
    }

    //获取当前登录用户创建的知识点列表 —— 仅教师及以上
    @GetMapping("/my")
    public Result<List<KnowledgePoint>> getMy() {
        assertTeacherOrAbove();
        Long creatorId = SecurityUtils.getUserId();
        return Result.success(knowledgePointService.getKnowledgePointsByCreatorId(creatorId));
    }

    //获取所有知识点列表 —— 仅教师及以上
    @GetMapping("/all")
    public Result<List<KnowledgePoint>> getAll() {
        assertTeacherOrAbove();
        return Result.success(knowledgePointService.getAllKnowledgePoints());
    }

    private void assertTeacherOrAbove() {
        if (!SecurityUtils.isTeacherOrAbove()) {
            throw new AccessDeniedException("学生无权访问知识点管理");
        }
    }
}

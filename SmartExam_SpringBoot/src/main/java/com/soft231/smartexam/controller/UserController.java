package com.soft231.smartexam.controller;

import com.soft231.smartexam.common.Result;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.entity.KnowledgePoint;
import com.soft231.smartexam.entity.Question;
import com.soft231.smartexam.entity.User;
import com.soft231.smartexam.entity.UserGroup;
import com.soft231.smartexam.service.ExamRecordService;
import com.soft231.smartexam.service.ExamService;
import com.soft231.smartexam.service.KnowledgePointService;
import com.soft231.smartexam.service.QuestionService;
import com.soft231.smartexam.service.UserGroupService;
import com.soft231.smartexam.service.UserService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

//用户管理控制器
//用户CRUD：创建 create / 查询 getById / 更新 update / 删除 delete / 列表 list
//搜索功能：关键词搜索用户列表 searchUsers
//说明：创建/删除/列表/搜索仅超管可用（SecurityConfig控制）；查询与更新支持本人自助，身份以JWT为准
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExamRecordService examRecordService;

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private UserGroupService userGroupService;

    //创建用户
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        User saved = userService.createUser(user);
        saved.setPassword(null);
        return Result.success(saved);
    }

    //根据ID查询用户（个人信息页自助查看）
    //非超级管理员只能查看本人资料，避免通过遍历ID读取他人信息
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        if (!SecurityUtils.isSuperAdmin() && !SecurityUtils.isSelf(id)) {
            throw new AccessDeniedException("只能查看本人信息");
        }
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    //更新用户信息 —— 超管可更新任意用户；其他用户只能更新本人，且不可修改角色
    @PutMapping
    public Result<User> update(@RequestBody User user) {
        if (!SecurityUtils.isSuperAdmin()) {
            user.setId(SecurityUtils.getUserId());
            //防止越权提权：role置null后updateById不会更新该字段
            user.setRole(null);
        }
        userService.updateUser(user);
        user.setPassword(null);
        return Result.success(user);
    }

    //删除用户 —— 仅超管可用（SecurityConfig控制）
    //用户是被多方引用的根实体，数据库外键对"考试记录/创建关系"均为RESTRICT：
    //这里先做引用校验给出可读提示，避免请求直接被数据库拒绝后返回409
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long recordCount = examRecordService.lambdaQuery()
                .eq(ExamRecord::getUserId, id).count();
        if (recordCount != null && recordCount > 0) {
            return Result.error(400, "该用户已有 " + recordCount
                    + " 份考试记录，删除会导致成绩无法追溯，禁止删除");
        }
        Long examCount = examService.lambdaQuery()
                .eq(Exam::getCreatorId, id).count();
        if (examCount != null && examCount > 0) {
            return Result.error(400, "该用户创建了 " + examCount + " 场考试，请先删除或转移后再删除该用户");
        }
        Long questionCount = questionService.lambdaQuery()
                .eq(Question::getCreatorId, id).count();
        if (questionCount != null && questionCount > 0) {
            return Result.error(400, "该用户创建了 " + questionCount + " 道题目，请先删除或转移后再删除该用户");
        }
        Long kpCount = knowledgePointService.lambdaQuery()
                .eq(KnowledgePoint::getCreatorId, id).count();
        if (kpCount != null && kpCount > 0) {
            return Result.error(400, "该用户创建了 " + kpCount + " 个知识点，请先删除或转移后再删除该用户");
        }
        Long groupCount = userGroupService.lambdaQuery()
                .eq(UserGroup::getCreatorId, id).count();
        if (groupCount != null && groupCount > 0) {
            return Result.error(400, "该用户创建了 " + groupCount + " 个用户组，请先删除或转移后再删除该用户");
        }
        userService.deleteUser(id);
        return Result.success();
    }

    //获取用户列表，支持关键词搜索
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword) {
        java.util.List<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userService.searchUsers(keyword.trim());
        } else {
            users = userService.list();
        }
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
}

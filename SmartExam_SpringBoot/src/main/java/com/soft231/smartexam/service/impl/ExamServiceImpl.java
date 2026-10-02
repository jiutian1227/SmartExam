package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.mapper.ExamMapper;
import com.soft231.smartexam.service.ExamService;
import com.soft231.smartexam.service.UserGroupMemberService;
import com.soft231.smartexam.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//考试服务实现类
//考试CRUD：创建考试 createExam / 用户可见考试列表 listForUser
//权限校验：检查用户访问权限 checkUserAccess
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    @Autowired
    private UserGroupMemberService userGroupMemberService;

    //创建考试
    @Override
    public Exam createExam(Exam exam) {
        this.save(exam);
        return exam;
    }

    //获取用户可参加的考试列表（分页）
    @Override
    public IPage<Exam> listForUser(IPage<Exam> page, Long userId) {
        return this.baseMapper.listForUser(page, userId);
    }

    //按角色综合判定试卷可见性：超管全部，教师仅自己创建，学生仅所属用户组
    @Override
    public boolean canAccess(Long examId, Long userId, Integer role) {
        Exam exam = this.getById(examId);
        if (exam == null || userId == null || role == null) {
            return false;
        }

        // 超级管理员：全部可见
        if (role == SecurityUtils.Role.SUPER_ADMIN.getCode()) {
            return true;
        }

        // 教师：仅自己创建的试卷
        if (role == SecurityUtils.Role.TEACHER.getCode()) {
            return exam.getCreatorId() != null && exam.getCreatorId().equals(userId);
        }

        // 学生：仅所属用户组已绑定的试卷
        return checkUserAccess(examId, userId);
    }

    //判断用户是否属于该试卷绑定的任一用户组（未绑定用户组一律不可见）
    @Override
    public boolean checkUserAccess(Long examId, Long userId) {
        Exam exam = this.getById(examId);
        if (exam == null) {
            return false;
        }

        String userGroupIds = exam.getUserGroupIds();
        // 未绑定任何用户组：学生不可见
        if (userGroupIds == null || userGroupIds.trim().isEmpty()) {
            return false;
        }

        List<Long> allowedGroupIds = new ArrayList<>();
        for (String id : userGroupIds.split(",")) {
            String trimmed = id.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                allowedGroupIds.add(Long.valueOf(trimmed));
            } catch (NumberFormatException ignored) {
                // 忽略脏数据，避免单条异常数据导致整场考试不可访问
            }
        }

        if (allowedGroupIds.isEmpty()) {
            return false;
        }

        // 只统计"本人所属 且 试卷已绑定"的用户组，避免拉取全部成员关系
        Long matched = userGroupMemberService.lambdaQuery()
                .eq(UserGroupMember::getUserId, userId)
                .in(UserGroupMember::getUserGroupId, allowedGroupIds)
                .count();

        return matched != null && matched > 0;
    }
}
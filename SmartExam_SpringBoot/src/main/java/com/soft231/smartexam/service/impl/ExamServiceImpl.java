package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.UserGroupMember;
import com.soft231.smartexam.mapper.ExamMapper;
import com.soft231.smartexam.service.ExamService;
import com.soft231.smartexam.service.UserGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    //检查用户是否有权限访问考试
    @Override
    public boolean checkUserAccess(Long examId, Long userId) {
        Exam exam = this.getById(examId);
        if (exam == null) {
            return false;
        }

        String userGroupIds = exam.getUserGroupIds();
        if (userGroupIds == null || userGroupIds.trim().isEmpty()) {
            return true;
        }

        List<Long> allowedGroupIds = new ArrayList<>();
        for (String id : userGroupIds.split(",")) {
            if (!id.trim().isEmpty()) {
                allowedGroupIds.add(Long.parseLong(id.trim()));
            }
        }

        if (allowedGroupIds.isEmpty()) {
            return true;
        }

        List<UserGroupMember> memberships = userGroupMemberService.lambdaQuery()
                .eq(UserGroupMember::getUserId, userId)
                .list();

        List<Long> userGroupIdList = memberships.stream()
                .map(UserGroupMember::getUserGroupId)
                .collect(Collectors.toList());

        for (Long allowedId : allowedGroupIds) {
            if (userGroupIdList.contains(allowedId)) {
                return true;
            }
        }

        return false;
    }
}
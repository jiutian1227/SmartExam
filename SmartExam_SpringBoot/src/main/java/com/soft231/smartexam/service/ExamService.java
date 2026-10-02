package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.Exam;

import java.util.List;

public interface ExamService extends IService<Exam> {

    Exam createExam(Exam exam);

    IPage<Exam> listForUser(IPage<Exam> page, Long userId);

    /**
     * 判断用户是否属于该试卷绑定的任一用户组：
     * 未绑定用户组（为空）一律返回false，学生只能看到本组试卷
     */
    boolean checkUserAccess(Long examId, Long userId);

    /**
     * 按角色综合判定试卷可见性：
     * 超级管理员(2)全部可见；教师(0)仅自己创建的；学生(1)仅所属用户组已绑定的
     */
    boolean canAccess(Long examId, Long userId, Integer role);
}
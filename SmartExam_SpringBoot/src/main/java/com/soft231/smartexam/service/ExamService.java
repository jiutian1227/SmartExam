package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.Exam;

import java.util.List;

public interface ExamService extends IService<Exam> {

    Exam createExam(Exam exam);

    IPage<Exam> listForUser(IPage<Exam> page, Long userId);

    boolean checkUserAccess(Long examId, Long userId);
}
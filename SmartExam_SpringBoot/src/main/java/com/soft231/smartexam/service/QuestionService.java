package com.soft231.smartexam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soft231.smartexam.entity.Question;

import java.util.List;

public interface QuestionService extends IService<Question> {

    //批量入库（AI出题勾选后一次性落库）——事务保证要么全进要么全不进
    //creatorId由服务端注入，并强制清空前端传来的id，防止越权覆盖他人题目
    int batchCreate(List<Question> questions, Long creatorId);
}

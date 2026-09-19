package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//答题记录Mapper接口
//查询答题记录（关联题目+考试分数） selectAnswersWithQuestion
@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    //查询答题记录（关联题目信息和考试题目分数）
    @Select("SELECT " +
            "ar.id, ar.exam_record_id, ar.question_id, ar.user_answer, ar.score, ar.comment, " +
            "q.content, q.type, q.options, q.answer, q.analysis, " +
            "eq.score as exam_score, " +
            "ar.create_time, ar.update_time " +
            "FROM answer_record ar " +
            "LEFT JOIN question q ON ar.question_id = q.id " +
            "LEFT JOIN exam_record er ON ar.exam_record_id = er.id " +
            "LEFT JOIN exam_question eq ON er.exam_id = eq.exam_id AND ar.question_id = eq.question_id " +
            "WHERE ar.exam_record_id = #{examRecordId}")
    List<AnswerRecordVO> selectAnswersWithQuestion(@Param("examRecordId") Long examRecordId);
}
package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

//答题记录Mapper接口
//查询答题记录（关联题目+考试分数） selectAnswersWithQuestion
@Mapper
public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    //查询答题记录（关联题目信息和考试题目分数）
    //按试卷题序排序：ORDER BY 依赖 exam_question.sort_order，保证教师批阅看到的题目顺序
    //与学生作答时看到的顺序一致；反之若靠 ar.id 隐式顺序，一旦草稿写入顺序与题序不同就会错位。
    //eq 为 LEFT JOIN，题目已被移出试卷时 sort_order 为 NULL，用 IS NULL 把这类行排到最后而不是最前。
    @Select("SELECT " +
            "ar.id, ar.exam_record_id, ar.question_id, ar.user_answer, ar.score, ar.comment, " +
            "q.content, q.type, q.options, q.answer, q.analysis, " +
            "eq.score as exam_score, " +
            "ar.create_time, ar.update_time " +
            "FROM answer_record ar " +
            "LEFT JOIN question q ON ar.question_id = q.id " +
            "LEFT JOIN exam_record er ON ar.exam_record_id = er.id " +
            "LEFT JOIN exam_question eq ON er.exam_id = eq.exam_id AND ar.question_id = eq.question_id " +
            "WHERE ar.exam_record_id = #{examRecordId} " +
            "ORDER BY eq.sort_order IS NULL, eq.sort_order, ar.id")
    List<AnswerRecordVO> selectAnswersWithQuestion(@Param("examRecordId") Long examRecordId);

    /**
     * 把某条答题记录的答案显式置为 NULL（保留该行本身）。
     * 必须写原生 SQL 而不能走 updateById：MyBatis-Plus 默认字段策略是 NOT_NULL，
     * 实体里 userAnswer=null 时这一列根本不会出现在 SET 子句里，置空会静默失效。
     */
    @Update("UPDATE answer_record SET user_answer = NULL WHERE id = #{id}")
    int clearUserAnswer(@Param("id") Long id);
}
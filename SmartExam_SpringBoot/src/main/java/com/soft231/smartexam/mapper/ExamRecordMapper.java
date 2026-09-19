package com.soft231.smartexam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;
import com.soft231.smartexam.entity.vo.ExamStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//考试记录Mapper接口
//用户查询：根据用户 IDselectByUserId / 查记录列表 selectRecordsWithInfoByUserId / 查记录详情 selectRecordWithInfoById
//考试查询：查考试所有记录 selectRecordsWithUserByExamId
//统计查询：按创建者 selectExamSubmissionStatsByCreatorId / 查询全部 selectAllExamSubmissionStats / 考试统计 selectExamStatsById
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    //根据用户ID查询考试记录
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId}")
    List<ExamRecord> selectByUserId(Long userId);

    //查询用户的考试记录列表（关联用户信息和考试信息）
    @Select("SELECT " +
            "er.id, er.exam_id, er.user_id, er.start_time, er.submit_time, er.score, er.status, " +
            "u.username, u.real_name, " +
            "e.title as exam_title, e.total_score, e.end_time, " +
            "CASE WHEN er.score IS NOT NULL THEN '已批阅' ELSE '待批阅' END as grading_status, " +
            "er.create_time, er.update_time " +
            "FROM exam_record er " +
            "LEFT JOIN user u ON er.user_id = u.id " +
            "LEFT JOIN exam e ON er.exam_id = e.id " +
            "WHERE er.user_id = #{userId} AND er.status = 2")
    List<ExamRecordVO> selectRecordsWithInfoByUserId(@Param("userId") Long userId);

    //查询考试记录（关联用户信息和考试信息）
    @Select("SELECT " +
            "er.id, er.exam_id, er.user_id, er.start_time, er.submit_time, er.score, er.status, " +
            "u.username, u.real_name, " +
            "e.title as exam_title, e.total_score, e.end_time, " +
            "CASE WHEN er.score IS NOT NULL THEN '已批阅' ELSE '待批阅' END as grading_status, " +
            "er.create_time, er.update_time " +
            "FROM exam_record er " +
            "LEFT JOIN user u ON er.user_id = u.id " +
            "LEFT JOIN exam e ON er.exam_id = e.id " +
            "WHERE er.id = #{recordId}")
    ExamRecordVO selectRecordWithInfoById(@Param("recordId") Long recordId);

    //查询考试的所有记录（关联用户信息）
    @Select("SELECT " +
            "er.id, er.exam_id, er.user_id, er.start_time, er.submit_time, er.score, er.status, " +
            "u.username, u.real_name, " +
            "CASE WHEN er.score IS NOT NULL THEN '已批阅' ELSE '待批阅' END as grading_status, " +
            "er.create_time, er.update_time " +
            "FROM exam_record er " +
            "LEFT JOIN user u ON er.user_id = u.id " +
            "WHERE er.exam_id = #{examId} " +
            "ORDER BY er.submit_time DESC")
    List<ExamRecordVO> selectRecordsWithUserByExamId(@Param("examId") Long examId);

    //查询考试提交统计（按创建者）
    @Select("SELECT " +
            "e.id, e.title, e.start_time, e.end_time, e.duration, e.total_score, e.user_group_ids, e.creator_id, " +
            "e.create_time, e.update_time, " +
            "(SELECT COUNT(DISTINCT ugm.user_id) FROM user_group_member ugm " +
            " WHERE FIND_IN_SET(ugm.user_group_id, e.user_group_ids)) as total_group_members, " +
            "(SELECT COUNT(*) FROM exam_record er2 WHERE er2.exam_id = e.id) as submitted_count, " +
            "(SELECT COUNT(*) FROM exam_record er3 WHERE er3.exam_id = e.id AND er3.score IS NOT NULL) as graded_count, " +
            "(SELECT COUNT(*) FROM exam_record er4 WHERE er4.exam_id = e.id AND er4.score IS NULL) as ungraded_count " +
            "FROM exam e WHERE e.creator_id = #{creatorId}")
    List<ExamSubmissionStatsVO> selectExamSubmissionStatsByCreatorId(@Param("creatorId") Long creatorId);

    //查询所有考试提交统计
    @Select("SELECT " +
            "e.id, e.title, e.start_time, e.end_time, e.duration, e.total_score, e.user_group_ids, e.creator_id, " +
            "e.create_time, e.update_time, " +
            "(SELECT COUNT(DISTINCT ugm.user_id) FROM user_group_member ugm " +
            " WHERE FIND_IN_SET(ugm.user_group_id, e.user_group_ids)) as total_group_members, " +
            "(SELECT COUNT(*) FROM exam_record er2 WHERE er2.exam_id = e.id) as submitted_count, " +
            "(SELECT COUNT(*) FROM exam_record er3 WHERE er3.exam_id = e.id AND er3.score IS NOT NULL) as graded_count, " +
            "(SELECT COUNT(*) FROM exam_record er4 WHERE er4.exam_id = e.id AND er4.score IS NULL) as ungraded_count " +
            "FROM exam e")
    List<ExamSubmissionStatsVO> selectAllExamSubmissionStats();

    //查询考试统计信息
    @Select("SELECT " +
            "e.id, e.title, e.description, e.start_time, e.end_time, e.duration, e.total_score, e.user_group_ids, e.creator_id, " +
            "e.create_time, e.update_time, " +
            "(SELECT COUNT(DISTINCT ugm.user_id) FROM user_group_member ugm " +
            " WHERE FIND_IN_SET(ugm.user_group_id, e.user_group_ids)) as total_group_members, " +
            "(SELECT COUNT(*) FROM exam_record er WHERE er.exam_id = e.id) as submitted_count, " +
            "(SELECT COUNT(*) FROM exam_record er2 WHERE er2.exam_id = e.id AND er2.score IS NOT NULL) as graded_count, " +
            "(SELECT COUNT(*) FROM exam_record er3 WHERE er3.exam_id = e.id AND er3.score IS NULL) as ungraded_count " +
            "FROM exam e WHERE e.id = #{examId}")
    ExamStatsVO selectExamStatsById(@Param("examId") Long examId);
}
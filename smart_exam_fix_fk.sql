-- ============================================================
-- SmartExam 增量迁移：补齐外键约束与二级索引
-- 适用场景：库已存在但建表时未带外键（information_schema.referential_constraints 为空）
-- 执行前必须确认无孤儿数据，否则 ALTER 会报 1452 外键冲突
-- 重复执行会提示 Duplicate key name / Duplicate foreign key，属正常，可忽略
-- ============================================================
USE `smart_exam`;

-- ---------- 0. 执行前置检查：以下查询应全部返回 0 ----------
-- SELECT COUNT(*) FROM exam_question eq LEFT JOIN exam e ON eq.exam_id=e.id WHERE e.id IS NULL;
-- SELECT COUNT(*) FROM exam_question eq LEFT JOIN question q ON eq.question_id=q.id WHERE q.id IS NULL;
-- SELECT COUNT(*) FROM exam_record er LEFT JOIN exam e ON er.exam_id=e.id WHERE e.id IS NULL;
-- SELECT COUNT(*) FROM exam_record er LEFT JOIN user u ON er.user_id=u.id WHERE u.id IS NULL;
-- SELECT COUNT(*) FROM answer_record ar LEFT JOIN exam_record er ON ar.exam_record_id=er.id WHERE er.id IS NULL;
-- SELECT COUNT(*) FROM answer_record ar LEFT JOIN question q ON ar.question_id=q.id WHERE q.id IS NULL;
-- SELECT COUNT(*) FROM question q LEFT JOIN knowledge_point kp ON q.knowledge_point_id=kp.id WHERE q.knowledge_point_id IS NOT NULL AND kp.id IS NULL;
-- SELECT COUNT(*) FROM user_group_member ugm LEFT JOIN user_group ug ON ugm.user_group_id=ug.id WHERE ug.id IS NULL;
-- SELECT COUNT(*) FROM user_group_member ugm LEFT JOIN user u ON ugm.user_id=u.id WHERE u.id IS NULL;

-- ---------- 1. 补二级索引（外键列必须先有索引）----------
-- 说明：answer_record.exam_record_id 已被 uk_record_question(exam_record_id, question_id) 最左列覆盖，无需重复建
--      user_group_member.user_group_id 已被 uk_group_user(user_group_id, user_id) 最左列覆盖，无需重复建
--      knowledge_point.creator_id 已有 idx_creator_id，无需重复建
ALTER TABLE `exam`              ADD INDEX `fk_exam_creator` (`creator_id`);
ALTER TABLE `exam_question`     ADD INDEX `fk_exam_question_exam` (`exam_id`);
ALTER TABLE `exam_question`     ADD INDEX `fk_exam_question_question` (`question_id`);
ALTER TABLE `exam_record`       ADD INDEX `fk_exam_record_exam` (`exam_id`);
ALTER TABLE `exam_record`       ADD INDEX `fk_exam_record_user` (`user_id`);
ALTER TABLE `answer_record`     ADD INDEX `fk_answer_record_question` (`question_id`);
ALTER TABLE `question`          ADD INDEX `fk_question_creator` (`creator_id`);
ALTER TABLE `question`          ADD INDEX `fk_question_knowledge_point` (`knowledge_point_id`);
ALTER TABLE `user_group`        ADD INDEX `fk_user_group_creator` (`creator_id`);
ALTER TABLE `user_group_member` ADD INDEX `fk_ugm_user` (`user_id`);

-- ---------- 2. 补外键 ----------
-- 级联策略说明（与 smart_exam.sql 完全一致）：
--   CASCADE  ：子表数据随主表一起删除，用于"离开主表就无意义"的附属数据（考试关联题、考试记录、答题明细、组成员）
--   RESTRICT ：只要被子表引用就禁止删除，用于"删除会造成数据不可追溯"的场景（题目、用户、知识点、用户组创建者）

-- 考试 → 创建者：教师账号不能被删，否则其试卷成为无主数据
ALTER TABLE `exam` ADD CONSTRAINT `fk_exam_creator`
    FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 考试-题目关联：随考试或题目删除而清理
ALTER TABLE `exam_question` ADD CONSTRAINT `fk_exam_question_exam`
    FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `exam_question` ADD CONSTRAINT `fk_exam_question_question`
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 考试记录：随考试删除级联；学生账号有作答记录时禁止删除，保证成绩可追溯
ALTER TABLE `exam_record` ADD CONSTRAINT `fk_exam_record_exam`
    FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `exam_record` ADD CONSTRAINT `fk_exam_record_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 答题明细：随记录删除级联；已被作答的题目禁止删除
ALTER TABLE `answer_record` ADD CONSTRAINT `fk_answer_record_exam_record`
    FOREIGN KEY (`exam_record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `answer_record` ADD CONSTRAINT `fk_answer_record_question`
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 题目：创建者与知识点均为 RESTRICT
ALTER TABLE `question` ADD CONSTRAINT `fk_question_creator`
    FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `question` ADD CONSTRAINT `fk_question_knowledge_point`
    FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 知识点、用户组：创建者为 RESTRICT
ALTER TABLE `knowledge_point` ADD CONSTRAINT `fk_knowledge_point_creator`
    FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `user_group` ADD CONSTRAINT `fk_user_group_creator`
    FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 用户组成员：组或用户消失后成员关系无意义，走 CASCADE
ALTER TABLE `user_group_member` ADD CONSTRAINT `fk_ugm_group`
    FOREIGN KEY (`user_group_id`) REFERENCES `user_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `user_group_member` ADD CONSTRAINT `fk_ugm_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- ---------- 3. 校验：应返回 13 ----------
-- SELECT COUNT(*) FROM information_schema.referential_constraints WHERE constraint_schema='smart_exam';

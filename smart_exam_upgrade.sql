-- ============================================================
-- 智能考试系统 —— 增量升级脚本
-- 适用：已经建过库、不想重建数据的环境（直接执行本文件即可）
-- 新建库的环境无需执行，smart_exam.sql 里已包含同样的约束
-- ============================================================

-- ------------------------------------------------------------
-- 升级 1：answer_record 增加唯一约束 (exam_record_id, question_id)
--
-- 背景：原表只建了两个普通索引，同一场考试同一道题可以插入多行。
--       历史实现里提交试卷是"逐题 INSERT"，一旦发生重复提交
--       就会写出重复答题记录，进而导致总分翻倍、批阅错乱。
--       加入唯一键后，服务端改为 upsert，重复上报天然幂等。
-- ------------------------------------------------------------

-- 步骤 1：先排查是否已存在重复行（执行后若有结果，先看第 2 步的清理语句）
SELECT exam_record_id, question_id, COUNT(*) AS dup_count
FROM answer_record
GROUP BY exam_record_id, question_id
HAVING COUNT(*) > 1;

-- 步骤 2：清理历史重复行，同一 (exam_record_id, question_id) 只保留 id 最大的一条
--         已提交的试卷若因重复提交产生了多行，保留最后写入的那条即为最新答案
DELETE a FROM answer_record a
JOIN (
    SELECT exam_record_id, question_id, MAX(id) AS keep_id
    FROM answer_record
    GROUP BY exam_record_id, question_id
    HAVING COUNT(*) > 1
) d ON a.exam_record_id = d.exam_record_id
   AND a.question_id = d.question_id
   AND a.id < d.keep_id;

-- 步骤 3：添加唯一索引
ALTER TABLE `answer_record`
    ADD UNIQUE INDEX `uk_record_question` (`exam_record_id` ASC, `question_id` ASC) USING BTREE;

-- ------------------------------------------------------------
-- 升级 2：试卷可见性 —— 批阅列表与统计口径修正
--
-- 背景：学生一旦点开试卷就会生成 exam_record（status=1 进行中），
--       原 SQL 未过滤状态，导致"进行中、未提交"的试卷也出现在
--       教师批阅列表里，统计里也被算作"已提交人数"。
--       本项为纯代码改动（见 ExamRecordMapper.java），无需执行 SQL。
-- ------------------------------------------------------------

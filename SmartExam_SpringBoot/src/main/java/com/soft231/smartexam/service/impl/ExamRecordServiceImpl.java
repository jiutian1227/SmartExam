package com.soft231.smartexam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft231.smartexam.entity.Exam;
import com.soft231.smartexam.entity.ExamRecord;
import com.soft231.smartexam.mapper.AnswerRecordMapper;
import com.soft231.smartexam.mapper.ExamMapper;
import com.soft231.smartexam.mapper.ExamRecordMapper;
import com.soft231.smartexam.service.AnswerRecordService;
import com.soft231.smartexam.service.ExamQuestionService;
import com.soft231.smartexam.service.ExamRecordService;
import com.soft231.smartexam.entity.vo.AnswerRecordVO;
import com.soft231.smartexam.entity.vo.ExamRecordVO;
import com.soft231.smartexam.entity.vo.ExamStatsVO;
import com.soft231.smartexam.entity.vo.ExamSubmissionStatsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soft231.smartexam.entity.AnswerRecord;
import com.soft231.smartexam.entity.ExamQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

//考试记录服务实现类
//考试流程：开始考试 startExam / 提交考试 submitExam / 创建记录 createRecord
//成绩查询：用户成绩列表 getRecordsWithExamInfoByUserId / 记录详情 getRecordById / 考试所有记录 getRecordsByExamId / 考试状态 getExamRecordStatus
//批阅打分：获取答题详情 getRecordAnswers / 自动批改客观题 autoGradeObjectiveQuestions / 提交分数 submitScores / 获取所有记录 getAllRecordsWithInfo
//记录管理：更新总分 updateTotalScore / 更新状态 updateRecordStatus / 已提交ID列表 getSubmittedExamIdsByUserId
//统计信息：考试提交统计 getExamSubmissionStats / 考试统计 getExamStats
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Autowired
    private AnswerRecordService answerRecordService;

    @Autowired
    private ExamQuestionService examQuestionService;
    
    /**
     * 提交宽限时间（秒）：容忍网络延迟与前端自动交卷的时序误差，
     * 超过"作答截止时间 + 宽限"后才真正拒收
     */
    private static final long SUBMIT_GRACE_SECONDS = 60L;

    //答案JSON解析器：全类共用一个实例，避免每次请求重复构造
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //日志：用于兜底交卷任务与草稿异常的排查
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamRecordServiceImpl.class);

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    //获取考试记录的答案列表
    @Override
    public List<Map<String, Object>> getRecordAnswers(Long recordId) {
        List<AnswerRecordVO> answers = answerRecordService.getAnswersWithQuestion(recordId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (AnswerRecordVO vo : answers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", vo.getId());
            item.put("examRecordId", vo.getExamRecordId());
            item.put("questionId", vo.getQuestionId());
            item.put("userAnswer", vo.getUserAnswer());
            item.put("score", vo.getScore());
            item.put("comment", vo.getComment());
            item.put("content", vo.getContent());
            item.put("type", vo.getType());
            item.put("options", vo.getOptions());
            item.put("answer", vo.getAnswer());
            item.put("analysis", vo.getAnalysis());
            item.put("examScore", vo.getExamScore() != null ? vo.getExamScore() : 10);
            result.add(item);
        }
        return result;
    }

    //获取用户的考试记录列表（数据库分页）
    @Override
    public IPage<ExamRecordVO> getRecordsWithExamInfoByUserId(IPage<ExamRecordVO> page, Long userId) {
        return baseMapper.selectRecordsWithInfoByUserIdPage(page, userId);
    }

    //根据ID获取考试记录详情
    @Override
    public ExamRecordVO getRecordById(Long id) {
        return baseMapper.selectRecordWithInfoById(id);
    }

    //获取用户已提交的考试ID列表
    @Override
    public List<Long> getSubmittedExamIdsByUserId(Long userId) {
        List<ExamRecord> records = baseMapper.selectByUserId(userId);

        return records.stream()
                .filter(record -> record.getStatus() == 2)
                .map(ExamRecord::getExamId)
                .distinct()
                .collect(Collectors.toList());
    }

    //更新考试记录的总分
    @Override
    public boolean updateTotalScore(Long recordId, Integer totalScore) {
        ExamRecord record = new ExamRecord();
        record.setId(recordId);
        record.setScore(totalScore);
        return this.updateById(record);
    }

    //更新考试记录状态
    @Override
    public boolean updateRecordStatus(Long recordId, Integer status) {
        ExamRecord record = new ExamRecord();
        record.setId(recordId);
        record.setStatus(status);
        return this.updateById(record);
    }

    //创建考试记录
    @Override
    public ExamRecord createRecord(ExamRecord record) {
        record.setStatus(1);
        record.setSubmitTime(java.time.LocalDateTime.now());
        this.save(record);
        return record;
    }

    //提交考试 —— 服务端强制限时：超时或重复提交一律拒收
    @Override
    @org.springframework.transaction.annotation.Transactional
    public ExamRecord submitExam(Long userId, Long examId, String answersJson) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("考试不存在");
        }

        // 加行锁读取这条记录：让"提交"与"保存草稿"两条写路径互斥。
        // 否则心跳草稿可能正写到一半，又并发进来一个提交，两边都判断"这道题还没有答案行"，
        // 于是都执行 INSERT，撞上 uk_record_question 唯一索引直接报错回滚。
        ExamRecord record = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1 FOR UPDATE")
                .one();

        // 必须先经过 startExam 开考，避免绕过开考时间直接写入答案
        if (record == null) {
            throw new IllegalArgumentException("请先开始考试后再提交");
        }
        // 已提交/已批阅的试卷不可重复提交，防止覆盖已判分数。
        // FOR UPDATE 读到的是最新已提交版本，所以并发进来的第二个请求一定会在这里看到 status=2。
        if (record.getStatus() != null && record.getStatus() >= 2) {
            throw new IllegalArgumentException("该试卷已提交，不可重复提交");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = computeDeadline(exam, record.getStartTime(), now);
        if (now.isAfter(deadline.plusSeconds(SUBMIT_GRACE_SECONDS))) {
            throw new IllegalArgumentException("作答时间已结束，无法提交");
        }

        if (record.getStartTime() == null) {
            record.setStartTime(now);
        }
        record.setSubmitTime(now);
        record.setStatus(2);
        this.saveOrUpdate(record);

        if (answersJson != null && !answersJson.isEmpty()) {
            // 按 (考试记录, 试题) 合并写入：已有草稿行走更新，避免重复提交写出重复行
            syncAnswerSnapshot(record.getId(), examId, answersJson);
        }

        return record;
    }

    /**
     * 兜底自动交卷：扫描仍处于"进行中"的记录，凡是已超过作答截止时间的直接置为已提交。
     * 必要性：学生直接关掉浏览器后，前端那三个交卷触发点都不会再执行，
     * 卷子会永远停在 status=1（既不会被批阅，也不再出现在教师批阅列表中）。
     * 已落库的草稿答案就是最终答案，因此这里只翻状态与提交时间，不重写答案。
     */
    @Override
    public int autoSubmitExpiredRecords() {
        List<ExamRecord> pending = this.lambdaQuery()
                .eq(ExamRecord::getStatus, 1)
                .list();
        if (pending.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int submitted = 0;
        for (ExamRecord record : pending) {
            try {
                // startTime 为空说明从未真正开考，跳过（deadline 会算成"现在+时长"，不会误判超时）
                if (record.getStartTime() == null) {
                    continue;
                }
                Exam exam = examMapper.selectById(record.getExamId());
                if (exam == null) {
                    continue;
                }
                LocalDateTime deadline = computeDeadline(exam, record.getStartTime(), now);
                if (!now.isAfter(deadline.plusSeconds(SUBMIT_GRACE_SECONDS))) {
                    continue;
                }

                // 同一考生同一场考试只应有一条记录，但并发开考（双开页面/连点）可能多插一条。
                // 若已有别的记录正式提交过，就不再为这条孤儿记录造出第二份"提交"，
                // 否则教师批阅列表里会出现同一学生的两份卷子。
                long siblingSubmitted = this.count(
                        new LambdaQueryWrapper<ExamRecord>()
                                .eq(ExamRecord::getUserId, record.getUserId())
                                .eq(ExamRecord::getExamId, record.getExamId())
                                .ne(ExamRecord::getId, record.getId())
                                .ge(ExamRecord::getStatus, 2));
                if (siblingSubmitted > 0) {
                    continue;
                }

                // 条件更新（CAS）：只有"此刻仍然是进行中"才翻状态。
                // 用户若刚好在这一瞬间点了交卷，这里会更新到 0 行，
                // 不会把用户真实的交卷时间覆盖成截止时间，也不会重复触发交卷。
                boolean won = this.lambdaUpdate()
                        .eq(ExamRecord::getId, record.getId())
                        .eq(ExamRecord::getStatus, 1)
                        .set(ExamRecord::getStatus, 2)
                        // 提交时间记为截止时间而非扫描时刻，避免任务延迟导致交卷时间偏晚
                        .set(ExamRecord::getSubmitTime, deadline)
                        .update();
                if (won) {
                    submitted++;
                }
            } catch (Exception e) {
                // 单条失败不影响其余记录，下一次扫描会重试
                LOGGER.warn("自动交卷失败 recordId={}", record.getId(), e);
            }
        }
        return submitted;
    }

    /**
     * 保存答题草稿（自动存卷的服务端落点）
     * 与"提交"共用同一套写入逻辑，唯一的区别是不改动 exam_record 的状态，
     * 因此学生仍在作答中，教师端不会把它当成已交卷的试卷。
     */
    @Override
    @Transactional
    public void saveDraft(Long userId, Long examId, String answersJson) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("考试不存在");
        }

        ExamRecord record = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1 FOR UPDATE")
                .one();

        if (record == null) {
            throw new IllegalArgumentException("请先开始考试");
        }
        // 双保险：只允许往自己的答题记录里写草稿
        if (!Objects.equals(record.getUserId(), userId)) {
            throw new AccessDeniedException("无权保存该试卷的草稿");
        }
        // 已交卷/已批阅的试卷不允许再通过草稿接口改写答案
        if (record.getStatus() != null && record.getStatus() >= 2) {
            throw new IllegalArgumentException("该试卷已提交，不能再保存草稿");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = computeDeadline(exam, record.getStartTime(), now);
        // 与提交同一套时间口径：超过"截止时间 + 宽限"后不再接受草稿，防止事后续答
        if (now.isAfter(deadline.plusSeconds(SUBMIT_GRACE_SECONDS))) {
            throw new IllegalArgumentException("作答时间已结束，无法保存草稿");
        }

        if (record.getStartTime() == null) {
            record.setStartTime(now);
            this.updateById(record);
        }

        syncAnswerSnapshot(record.getId(), examId, answersJson);
    }

    /**
     * 把一次完整的答案快照写入 answer_record。
     * 前端每次上报的都是"全部题目"的答案，因此本方法以快照为准：
     * 有答案的题 upsert，本次未作答的题把 user_answer 置空（保留行本身），保证重复上报幂等。
     * 只维护 user_answer 字段，不触碰 score / comment，避免覆盖教师已判的分数。
     */
    private void syncAnswerSnapshot(Long examRecordId, Long examId, String answersJson) {
        Map<String, Object> answersMap;
        if (answersJson == null || answersJson.isEmpty()) {
            answersMap = Collections.emptyMap();
        } else {
            try {
                answersMap = OBJECT_MAPPER.readValue(answersJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                throw new IllegalArgumentException("答案格式不正确");
            }
        }

        List<ExamQuestion> examQuestions = examQuestionService.list(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, examId)
                        .orderByAsc(ExamQuestion::getSortOrder));
        if (examQuestions.isEmpty()) {
            return;
        }

        // 一次查出该记录已有答案，按题号建索引，避免逐题查询
        Map<Long, AnswerRecord> existing = new HashMap<>();
        for (AnswerRecord ar : answerRecordService.list(
                new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getExamRecordId, examRecordId))) {
            existing.put(ar.getQuestionId(), ar);
        }

        for (ExamQuestion eq : examQuestions) {
            Long questionId = eq.getQuestionId();
            String answerStr = toAnswerString(answersMap.get(String.valueOf(questionId)));
            AnswerRecord current = existing.get(questionId);

            if (answerStr == null) {
                // 本题本次未作答：把已有行置空，而不是删除整行。
                // 删行会让这道题从教师判卷页消失，与"开考预建空行"的设计直接冲突；
                // 置空则保留题目行，教师能看到"未作答"并正常计 0 分。
                if (current != null) {
                    if (current.getUserAnswer() != null) {
                        answerRecordMapper.clearUserAnswer(current.getId());
                    }
                } else {
                    // 老记录（开考时还没预建空行）走到这里：补一条空行，
                    // 让历史试卷在判卷页同样能显示完整题目清单
                    AnswerRecord ar = new AnswerRecord();
                    ar.setExamRecordId(examRecordId);
                    ar.setQuestionId(questionId);
                    ar.setUserAnswer(null);
                    answerRecordService.save(ar);
                }
            } else if (current != null) {
                current.setUserAnswer(answerStr);
                answerRecordService.updateById(current);
            } else {
                AnswerRecord ar = new AnswerRecord();
                ar.setExamRecordId(examRecordId);
                ar.setQuestionId(questionId);
                ar.setUserAnswer(answerStr);
                answerRecordService.save(ar);
            }
        }
    }

    /**
     * 把前端传来的答案值统一转成入库字符串，未作答统一返回 null。
     * 多选/多空按逗号拼接，与自动批改的解析口径保持一致。
     */
    private String toAnswerString(Object userAnswer) {
        if (userAnswer == null) {
            return null;
        }
        if (userAnswer instanceof List) {
            List<?> list = (List<?>) userAnswer;
            if (list.isEmpty()) {
                return null;
            }
            return list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        String value = String.valueOf(userAnswer);
        return value.isEmpty() ? null : value;
    }

    //提交分数
    @Override
    @Transactional
    public void submitScores(Long recordId, List<Map<String, Object>> scoreList) {
        answerRecordService.batchUpdateScores(scoreList);

        int totalScore = scoreList.stream()
                .mapToInt(item -> ((Number) item.get("score")).intValue())
                .sum();
        this.updateTotalScore(recordId, totalScore);
        this.updateRecordStatus(recordId, 2);
    }

    //获取考试提交统计信息（分页）
    @Override
    public IPage<ExamSubmissionStatsVO> getExamSubmissionStats(IPage<ExamSubmissionStatsVO> page, Long creatorId) {
        if (creatorId != null) {
            return baseMapper.selectExamSubmissionStatsByCreatorIdPage(page, creatorId);
        }
        return baseMapper.selectAllExamSubmissionStatsPage(page);
    }

    //获取考试提交统计信息（全量，不分页）
    @Override
    public List<ExamSubmissionStatsVO> getExamSubmissionStatsList(Long creatorId) {
        if (creatorId != null) {
            return baseMapper.selectExamSubmissionStatsByCreatorId(creatorId);
        }
        return baseMapper.selectAllExamSubmissionStats();
    }

    //获取考试的所有记录（分页）
    @Override
    public IPage<ExamRecordVO> getRecordsByExamId(IPage<ExamRecordVO> page, Long examId) {
        return baseMapper.selectRecordsWithUserByExamIdPage(page, examId);
    }

    //获取考试统计信息
    @Override
    public ExamStatsVO getExamStats(Long examId) {
        return baseMapper.selectExamStatsById(examId);
    }

    //获取用户的考试状态
    @Override
    public Map<String, Object> getExamRecordStatus(Long userId, Long examId, boolean withAnswers) {
        ExamRecord record = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        Map<String, Object> result = new HashMap<>();
        // 服务端当前时间戳，供前端校准本地时钟，防止改系统时间延长作答
        result.put("serverTime", System.currentTimeMillis());

        if (record != null) {
            result.put("hasRecord", true);
            result.put("recordId", record.getId());
            result.put("startTime", record.getStartTime());
            result.put("status", record.getStatus());

            Exam exam = examMapper.selectById(examId);
            if (exam != null && record.getStartTime() != null) {
                LocalDateTime deadline = computeDeadline(exam, record.getStartTime(), LocalDateTime.now());
                result.put("deadline", deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                // 已提交的没有剩余时间；未提交的按服务端时间计算
                long remain = record.getStatus() != null && record.getStatus() == 2
                        ? 0
                        : Duration.between(LocalDateTime.now(), deadline).getSeconds();
                result.put("remainSeconds", Math.max(0, remain));
            }

            // 回带已保存的答案，供前端刷新页面 / 换设备后恢复作答
            // 心跳每 30 秒调一次本接口，仅在页面加载（withAnswers=true）时才带答案，避免反复传输长文本
            if (withAnswers) {
                result.put("answers", loadSavedAnswers(record.getId()));
            }
        } else {
            result.put("hasRecord", false);
            result.put("recordId", null);
            result.put("startTime", null);
            result.put("status", null);
            if (withAnswers) {
                result.put("answers", Collections.emptyMap());
            }
        }
        return result;
    }

    /**
     * 读取已经落库的答案，并还原成前端作答状态的原格式：
     * 单选→选项下标，多选→下标数组，判断→0/1，填空与简答→原字符串。
     * 这样前端拿到后可以直接覆盖 answers 对象，无需再做一次转换。
     */
    private Map<String, Object> loadSavedAnswers(Long examRecordId) {
        Map<String, Object> restored = new HashMap<>();
        for (AnswerRecordVO vo : answerRecordService.getAnswersWithQuestion(examRecordId)) {
            Object value = restoreAnswerValue(vo.getUserAnswer(), vo.getType());
            if (value != null) {
                restored.put(String.valueOf(vo.getQuestionId()), value);
            }
        }
        return restored;
    }

    /**
     * 把入库字符串还原为前端答案值，无法识别时返回 null（视为未作答）
     */
    private Object restoreAnswerValue(String stored, Integer type) {
        if (stored == null || stored.isEmpty()) {
            return null;
        }
        if (type == null) {
            return stored;
        }
        switch (type) {
            case 0: {
                // 单选："A" → 0
                int index = stored.charAt(0) - 'A';
                return index >= 0 ? index : null;
            }
            case 1: {
                // 多选："A,C" → [0, 2]
                List<Integer> indexes = new ArrayList<>();
                for (String part : stored.split(",")) {
                    String option = part.trim();
                    if (option.isEmpty()) {
                        continue;
                    }
                    int index = option.charAt(0) - 'A';
                    if (index >= 0) {
                        indexes.add(index);
                    }
                }
                return indexes.isEmpty() ? null : indexes;
            }
            case 2: {
                // 判断："正确" → 0，"错误" → 1
                String value = stored.trim();
                if ("正确".equals(value)) {
                    return 0;
                }
                return "错误".equals(value) ? 1 : null;
            }
            default:
                // 填空 / 简答：原样返回
                return stored;
        }
    }

    //开始考试
    //开考 = 建考试记录 + 为试卷每题预建一条空答题记录，两步必须同时成功
    @Override
    @Transactional
    public ExamRecord startExam(Long userId, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("考试不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        // 未到开考时间不允许进入
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new IllegalArgumentException("考试尚未开始");
        }
        if (exam.getEndTime() != null) {
            if (now.isAfter(exam.getEndTime())) {
                throw new IllegalArgumentException("考试已结束，不可再进入");
            }
        }
        
        ExamRecord existingRecord = this.lambdaQuery()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getExamId, examId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
        
        if (existingRecord != null) {
            if (existingRecord.getStatus() == 2) {
                throw new IllegalArgumentException("该考试已提交，不可再进入");
            }
            // 中途刷新/重进：沿用原作答记录，倒计时不会重置
            return existingRecord;
        }
        
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setStartTime(java.time.LocalDateTime.now());
        record.setStatus(1);
        this.save(record);
        // 预建空答题行：保证教师判卷页能拿到完整的题目清单（含学生漏答的题）
        initAnswerRows(record.getId(), examId);
        return record;
    }

    /**
     * 开考时为该试卷的每一道题预建一条空答题记录（user_answer 为 NULL）。
     * 必要性：批阅查询 selectAnswersWithQuestion 以 answer_record 为主表，
     * 若只在学生作答时才建行，漏答的题在教师判卷页会凭空消失，
     * 教师既看不到这道题，也无法区分"学生漏答"与"该题不在卷子里"。
     * 预建后每道题都有稳定的 answer_record.id，提交分数走的便全是 UPDATE，
     * 顺带消除了"草稿与提交并发 INSERT 撞 uk_record_question"的隐患。
     */
    private void initAnswerRows(Long examRecordId, Long examId) {
        List<ExamQuestion> examQuestions = examQuestionService.list(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId));
        if (examQuestions.isEmpty()) {
            return;
        }
        List<AnswerRecord> rows = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            AnswerRecord ar = new AnswerRecord();
            ar.setExamRecordId(examRecordId);
            ar.setQuestionId(eq.getQuestionId());
            ar.setUserAnswer(null);
            rows.add(ar);
        }
        answerRecordService.saveBatch(rows);
    }

    /**
     * 计算该考生的作答截止时间：min(考试统一结束时间, 开考时间 + 限时时长)
     */
    private LocalDateTime computeDeadline(Exam exam, LocalDateTime startAt, LocalDateTime now) {
        LocalDateTime base = startAt != null ? startAt : now;
        int duration = exam.getDuration() == null ? 0 : exam.getDuration();
        LocalDateTime deadline = base.plusMinutes(duration);
        if (exam.getEndTime() != null && exam.getEndTime().isBefore(deadline)) {
            deadline = exam.getEndTime();
        }
        return deadline;
    }

    //自动批改客观题
    @Override
    public List<Map<String, Object>> autoGradeObjectiveQuestions(Long recordId) {
        List<AnswerRecordVO> answers = answerRecordService.getAnswersWithQuestion(recordId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AnswerRecordVO answer : answers) {
            Map<String, Object> item = new HashMap<>();
            Integer type = answer.getType();
            Long answerId = answer.getId();
            String userAnswer = answer.getUserAnswer();
            String correctAnswer = answer.getAnswer();
            Integer score = answer.getExamScore();
            Integer currentScore = answer.getScore();

            item.put("id", answerId);

            // 只判单选(0)/多选(1)/判断(2)。填空(3)与简答(4)表述自由，归为主观题交给 AI 分析
            if (type != null && (type == 0 || type == 1 || type == 2)) {
                boolean isCorrect = false;
                Integer finalScore = 0;
                // 客观题缺少标准答案
                boolean missingAnswer = correctAnswer == null || correctAnswer.trim().isEmpty();
                // 未作答
                boolean notAnswered = userAnswer == null || userAnswer.trim().isEmpty();
                if (!missingAnswer && userAnswer != null && !userAnswer.isEmpty()) {
                    if (type == 1) {
                        String[] userArr = userAnswer.split(",");
                        String[] correctArr = correctAnswer.split(",");
                        java.util.Set<String> userSet = new java.util.HashSet<>();
                        java.util.Set<String> correctSet = new java.util.HashSet<>();
                        for(String s : userArr){
                            String trim = s.trim();
                            if(!trim.isEmpty()) userSet.add(trim);
                        }
                        for(String s : correctArr){
                            String trim = s.trim();
                            if(!trim.isEmpty()) correctSet.add(trim);
                        }

                        boolean hasWrongAnswer = false;
                        for (String userOpt : userSet) {
                            if (!correctSet.contains(userOpt)) {
                                hasWrongAnswer = true;
                                break;
                            }
                        }

                        if (!hasWrongAnswer) {
                            // 无错误选项
                            boolean allMatch = true;
                            for(String c : correctSet){
                                if(!userSet.contains(c)){
                                    allMatch = false;
                                    break;
                                }
                            }
                            if(allMatch){
                                // 全对，满分
                                finalScore = score;
                                isCorrect = true;
                            }else{
                                // 少选部分正确，给一半分
                                finalScore = score / 2;
                                isCorrect = false;
                            }
                        }
                        // 有错选：finalScore保持0
                    } else {
                        // 单选(0) / 判断(2)
                        isCorrect = userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
                        if (isCorrect) {
                            finalScore = score;
                        }
                    }
                }

                item.put("score", finalScore);
                item.put("isObjective", true);
                item.put("isCorrect", isCorrect);
                item.put("missingAnswer", missingAnswer);
                item.put("notAnswered", notAnswered);
            } else {
                item.put("score", currentScore != null ? currentScore : 0);
                item.put("isObjective", false);
                item.put("isCorrect", false);
            }

            item.put("comment", answer.getComment());
            result.add(item);
        }

        return result;
    }

    //获取所有考试记录
    @Override
    public IPage<ExamRecordVO> getAllRecordsWithInfo(IPage<ExamRecordVO> page) {
        return baseMapper.selectAllRecordsWithInfoPage(page);
    }
}
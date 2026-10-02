package com.soft231.smartexam.task;

import com.soft231.smartexam.service.ExamRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 考试兜底定时任务
 *
 * 背景：自动交卷原本只有前端三个触发点（本地倒计时归零、心跳发现服务端剩余时间归零、用户点退出）。
 * 这些都依赖浏览器还开着——学生直接关掉浏览器、拔网线或直接关机，
 * 卷子就会永远停在 status=1（进行中）：既不会被批阅，也不会出现在教师批阅列表里。
 *
 * 因此由服务端每分钟扫一次，把已经超过作答截止时间的记录强制交卷。
 * 已保存的草稿答案即为最终答案，任务不需要也不应该重写答案。
 */
@Component
public class ExamAutoSubmitTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamAutoSubmitTask.class);

    @Autowired
    private ExamRecordService examRecordService;

    /**
     * 启动 15 秒后执行一次（顺带清掉上次停服期间遗留的超时卷），之后每 60 秒执行一次。
     * 60 秒的间隔足够：交卷时间的记录口径取的是"截止时间"而非扫描时刻，不会因为延迟而不准。
     */
    @Scheduled(initialDelay = 15_000, fixedDelay = 60_000)
    public void autoSubmitExpiredExams() {
        try {
            int count = examRecordService.autoSubmitExpiredRecords();
            if (count > 0) {
                LOGGER.info("兜底自动交卷：本次处理 {} 份超时未提交的试卷", count);
            }
        } catch (Exception e) {
            // 任务异常不能让调度线程退出，否则后续所有交卷都不会再执行
            LOGGER.error("兜底自动交卷任务执行失败", e);
        }
    }
}

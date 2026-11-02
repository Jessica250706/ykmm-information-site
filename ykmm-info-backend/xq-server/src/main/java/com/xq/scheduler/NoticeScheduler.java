package com.xq.scheduler;

import com.xq.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 公告定时任务
 * <p>
 * 两个独立任务：
 * <ul>
 *   <li>{@link #publishScheduledNotices()}：把到期的「待发布」改为「已发布」</li>
 *   <li>{@link #expireOutdatedNotices()}：把过期的「已发布」改为「已下线」</li>
 * </ul>
 * 分开的好处：
 * <ul>
 *   <li>日志清晰，能区分"发布了几条"和"下架了几条"</li>
 *   <li>频率可以独立调整（当前都是 30 秒，未来如需可分开）</li>
 *   <li>将来加"过期前 N 天提醒"等扩展时，不影响已有逻辑</li>
 * </ul>
 * 使用 fixedDelay 而非 fixedRate：保证上一次执行完全结束后再等 30 秒，
 * 避免任务堆积。个人项目公告量小，30 秒精度对用户无感知。
 * 将来公告量很大时，可以改成分批 LIMIT 1000 的方式避免长事务。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NoticeScheduler {

    private final NoticeMapper noticeMapper;

    /**
     * 定时发布公告：
     * 把 status = 3（待发布）且 publish_at 已到达的改为 status = 1（已发布）
     * <p>
     * 只改 status，不动 publish_at——用户看到的"发布时间"应等于他设置的计划时间。
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void publishScheduledNotices() {
        try {
            int count = noticeMapper.publishScheduledNow();
            if (count > 0) {
                log.info("定时发布公告：本次共发布 {} 条", count);
            }
        } catch (Exception e) {
            // 定时任务出错必须自己 catch，否则会影响后续调度
            log.error("定时发布公告失败", e);
        }
    }

    /**
     * 定时下架过期公告：
     * 把 status = 1（已发布）且 expire_at 已到达的改为 status = 2（已下线）
     * <p>
     * 只改 status，不动 expire_at——便于后台追溯"原本计划何时下架"。
     * <p>
     * 频率同样是 30 秒：用户设置的过期时间精度是秒，
     * 30 秒内的延迟用户完全感知不到（过期多挂 30 秒不会造成任何业务影响）。
     */
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void expireOutdatedNotices() {
        try {
            int count = noticeMapper.expireOutdatedNow();
            if (count > 0) {
                log.info("定时下架过期公告：本次共下架 {} 条", count);
            }
        } catch (Exception e) {
            log.error("定时下架过期公告失败", e);
        }
    }
}

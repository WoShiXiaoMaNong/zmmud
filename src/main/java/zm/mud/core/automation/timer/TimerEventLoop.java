package zm.mud.core.automation.timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.thread.ZmmudThreadPools;

@Service
public class TimerEventLoop {
    private static final Logger logger = LogManager.getLogger(TimerEventLoop.class);
    @Autowired
    private TimerManager timerManager;

    public void start() {
        logger.info("TimerEveentLoop start!");
        this.timerManager.reloadTimer();

        ZmmudThreadPools.MUD_TIMER.execute(() -> {
            logger.info("TimerEveentLoop Thread start!");
            while (true) {
                // 1. 驱动 Timer 执行
                timerManager.tick();

                // 2. 计算下一次该睡多久
                long nextDelay = timerManager.getNextDelay();

                try {
                    if (nextDelay < 0) {
                        // 没有 Timer，可以长睡，或者由网络事件唤醒
                        Thread.sleep(100);
                    } else if (nextDelay > 0) {
                        Thread.sleep(nextDelay);
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        break;
                    } else {
                        logger.error("Timer error!", e);
                    }

                }
            }
            logger.info("TimerEveentLoop end!");
        });

    }

}

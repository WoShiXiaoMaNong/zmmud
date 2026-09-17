package zm.mud.core.automation.timer.timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;



@Service
public class TimerEventLoop {
    private static final Logger logger = LogManager.getLogger(TimerEventLoop.class);
    @Autowired
    private TimerManager timerManager;

    @PostConstruct 
    public void start() {
        logger.info("TimerEveentLoop start!");
        this.timerManager.reloadTimer();

        Thread timerThread = new Thread(() -> {
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
        }, "Timer-Refresh-Thread");
        timerThread.setDaemon(true); // 设置为守护线程，应用退出时自动销毁
        timerThread.start();
    }

}

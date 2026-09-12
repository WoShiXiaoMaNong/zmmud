package zm.mud.core.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ZMMud 全局统一线程池（单例）
 * 统一管理 MUD 客户端的所有异步任务（触发器、网络发送、UI异步渲染、图片下载等）
 */
public class ZmmudThreadPool {

    private static final Logger logger = LogManager.getLogger(ZmmudThreadPool.class);
    
    // 自动获取 CPU 核心数，作为动态调整的基准
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    // 核心线程数：保持与 CPU 核心数一致（MUD 任务多为计算密集型的文本正则匹配）
    private static final int CORE_POOL_SIZE = Math.max(2, CPU_CORES);
    // 最大线程数：允许在 I/O 密集（如图片下载、网络卡顿）时阶段性扩展
    private static final int MAX_POOL_SIZE = Math.max(8,CORE_POOL_SIZE * 2);
    // 缓冲队列：由于是单玩家客户端，1000 的容量足以支撑瞬间爆发的爆发性指令（如批量行走、大段文本触发）
    private static final int QUEUE_CAPACITY = 1000;

    private static final ThreadPoolExecutor EXECUTOR;
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);

    static {
        logger.info("正在初始化 ZMMud 全局统一线程池... (Core: {}, Max: {})", CORE_POOL_SIZE, MAX_POOL_SIZE);
        
        EXECUTOR = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                30L, TimeUnit.SECONDS, // 缩短空闲线程存活时间，更快释放内存
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                runnable -> {
                    Thread t = new Thread(runnable, "ZmMud-Worker-" + THREAD_NUMBER.getAndIncrement());
                    t.setDaemon(true); // 守护线程，主程序关闭时自动销毁
                    return t;
                },
                // 使用 DiscardOldestPolicy：如果队列爆满，抛弃最老的任务（通常是旧的过时触发文本），
                // 优先处理最新的服务器事件。这对于 MUD 客户端,需要防卡死
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        
        // 允许核心线程在无任务时超时销毁
        EXECUTOR.allowCoreThreadTimeOut(true);
    }

    /**
     * 执行无返回值的异步任务
     */
    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }

    /**
     * 执行带超时控制的任务（如特定的网络命令等待或耗时脚本）
     */
    public static void executeWithTimeout(Runnable task, long timeout, TimeUnit unit) {
        Future<?> future = EXECUTOR.submit(task);
        try {
            future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true); // 超时中断
            logger.warn("任务执行超时({} {}), 已强行取消！", timeout, unit);
        } catch (InterruptedException e) {
            logger.error("任务在等待期间被中断");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("任务执行过程中抛出异常: ", e.getCause());
        }
    }


    public static void executeInNewThread(Runnable task, long timeout, TimeUnit unit) {
        if (task == null) return;

        // 1. 利用现有的线程工厂创建一个独立的工人线程
        Thread workerThread = EXECUTOR.getThreadFactory().newThread(task);
        workerThread.start();

        // 2. 利用现有的线程池，异步启动一个“看门狗”任务来监控这个工人线程的超时
        EXECUTOR.execute(() -> {
            try {
                // 等待指定的超时时间
                unit.sleep(timeout);
                
                // 如果时间到了，工人线程还在运行，则强行中断它
                if (workerThread.isAlive()) {
                    workerThread.interrupt();
                    logger.warn("独立线程 [{}] 执行超时({} {}), 已向其发送中断信号！", 
                            workerThread.getName(), timeout, unit);
                }
            } catch (InterruptedException e) {
                // 看门狗自身被中断，通常发生在主程序关闭时
                Thread.currentThread().interrupt();
            }
        });
    }
    /**
     * 获取原生线程池实例（用于特殊监控或高级操作）
     */
    public static ThreadPoolExecutor getExecutor() {
        return EXECUTOR;
    }


    public static void shutdown() {
        logger.info("正在关闭 ZMMud 全局线程池...");
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

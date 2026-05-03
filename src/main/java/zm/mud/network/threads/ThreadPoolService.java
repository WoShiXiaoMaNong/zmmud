package zm.mud.network.threads;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ThreadPoolService implements DisposableBean {
    private static final Logger logger = LogManager.getLogger(ThreadPoolService.class);


    private int threadPoolSize ;
    private ExecutorService executor ;

    @Autowired
    private List<IZmmudThread> threads;

    private volatile boolean started = false;

    @PostConstruct
    public void init() {
        this.threadPoolSize = Math.max(1, this.threads.size()); // 确保线程池大小至少为1
        logger.info("ThreadPoolService initialized with thread pool size: {}", threadPoolSize);
        executor = Executors.newFixedThreadPool(threadPoolSize, r -> {
        Thread t = new Thread(r);
        t.setDaemon(false);
        return t;
    });
    }

    public synchronized void startAllThreads() {
        if (started) {
            logger.warn("Sub-threads have already been started.");
            return;
        }
        started = true;
        logger.info("Starting all sub-threads...");
        for (IZmmudThread thread : threads) {
             executor.submit(thread);
            logger.info("Started thread: " + thread.getClass().getSimpleName());
        }
    }

  

    @Override
    public void destroy() {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        logger.info("Shutting down thread pool...");   
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate within 5 seconds");
            }
        } catch (InterruptedException e) {
            logger.error("Error occurred while waiting for executor to terminate", e);
            Thread.currentThread().interrupt();
        }
    }


}

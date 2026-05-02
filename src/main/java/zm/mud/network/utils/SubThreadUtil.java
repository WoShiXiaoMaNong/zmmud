package zm.mud.network.utils;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.threads.IZmmudThread;

@Service
public class SubThreadUtil implements DisposableBean {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(SubThreadUtil.class);

    @Autowired
    private List<IZmmudThread> threads;

    private List<Thread> workerThreads;

    private volatile boolean started = false;

    public SubThreadUtil() {
        this.workerThreads = new java.util.ArrayList<>();
    }

    public synchronized void startAllThreads() {
        if (started) {
            logger.warn("Sub-threads have already been started.");
            return;
        }
        started = true;
        logger.info("Starting all sub-threads...");
        for (IZmmudThread thread : threads) {
            Thread workerThread = new Thread(thread, thread.getClass().getSimpleName() + "-Thread");
            workerThread.start();
            workerThreads.add(workerThread);
            logger.info("Started thread: " + thread.getClass().getSimpleName());
        }
    }

    public synchronized void shutdownAllThreads() {
        if (!started) {
            logger.warn("Sub-threads have not been started yet.");
            return;
        }
        started = false;
        logger.info("Shutting down all sub-threads...");
        for (IZmmudThread thread : threads) {
            thread.shutdown();
            logger.info("Shutdown signal sent to thread: " + thread.getClass().getSimpleName());
        }
        for (Thread workerThread : workerThreads) {
            try {
                if (workerThread.isAlive()) {
                    workerThread.interrupt();
                }
                workerThread.join(3000); // Wait for the thread to finish, with a timeout
                logger.info("Thread joined: " + workerThread.getName());
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for thread to finish", e);
                Thread.currentThread().interrupt();
            }
        }
        this.workerThreads.clear();
        logger.info("All sub-threads have been shut down.");
    }

    @Override
    public void destroy() throws Exception {
        shutdownAllThreads();
    }

}

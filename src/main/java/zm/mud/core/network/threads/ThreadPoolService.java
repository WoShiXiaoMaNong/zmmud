package zm.mud.core.network.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;


import zm.mud.core.session.MudSession;

@Service
public class ThreadPoolService implements DisposableBean {
    private static final Logger logger = LogManager.getLogger(ThreadPoolService.class);

    private Map<String/* SessionId */, ExecutorService> executorMap;

    private Map<String/* SessionId */, List<IZmmudThread>> threadsMap;

    private volatile Map<String/* SessionId */, Boolean> startFlagMap;

    public ThreadPoolService() {
        this.executorMap = new ConcurrentHashMap<>();
        this.threadsMap = new ConcurrentHashMap<>();
        this.startFlagMap = new ConcurrentHashMap<>();
    }

    private ExecutorService initForSession(MudSession session, int threadPoolSize) {
        // this.threadPoolSize = Math.max(1, this.threads.size()); // 确保线程池大小至少为1
        logger.info("ThreadPoolService initialized with thread pool size: {}", threadPoolSize);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            return t;
        });
        executorMap.put(session.getSessionId(), executor);
        return executor;
    }

    public synchronized void startAllThreads(MudSession session) {
        Boolean startFlag= startFlagMap.get(session.getSessionId());
        boolean started = startFlag != null && startFlag;
        if ( started) {
            logger.warn("Sub-threads have already been started.");
            return;
        }
        startFlagMap.put(session.getSessionId(), true);
        List<IZmmudThread> threads = this.buildThreads(session);
        if (threads == null || threads.isEmpty()) {
            logger.warn("Sub-threads is empty.");
            return;
        }
        int threadPoolSize = Math.max(1, threads.size()); // 确保线程池大小至少为1
        ExecutorService executor = this.initForSession(session, threadPoolSize);
        logger.info("Starting all sub-threads...");
        for (IZmmudThread thread : threads) {
            executor.submit(thread);
            logger.info("Started thread: " + thread.getClass().getSimpleName());
        }
    }

    private List<IZmmudThread> buildThreads(MudSession session) {
        List<IZmmudThread> threads = new ArrayList<>();
        threads.add(new InbMsgProcessThread(session));
        threads.add(new InbReadThread(session));
        threads.add(new OubMsgProcessThread(session));
        threadsMap.put(session.getSessionId(),threads);
        return threads;
    }

    

    public void shutdownAll() {
        for( Entry<String,List<IZmmudThread>> entry : this.threadsMap.entrySet()){
            String sessionId = entry.getKey();
            MudSession session = MudSession.getSession(sessionId);
            if(session == null){
                continue;
            }
            this.shutdown(session);
        }
        
    }
    public void shutdown(MudSession session) {
        List<IZmmudThread> threads = this.threadsMap.remove(session.getSessionId());
        if(threads == null || threads.isEmpty() ){
            return;
        }
        for(IZmmudThread t : threads){
            t.shutdown();
        }
    }

    @Override
    public void destroy() {
        for (Entry<String, ExecutorService> entry : this.executorMap.entrySet()) {
            ExecutorService executor = entry.getValue();
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

}

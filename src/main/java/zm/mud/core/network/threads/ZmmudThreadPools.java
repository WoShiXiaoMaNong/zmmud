package zm.mud.core.network.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ZmmudThreadPools {
   

    MUD_TRRIGER_MATCH("Mud Trigger Match",1,4),
    MUD_TRRIGER_ACTION("Mud Trigger Action",1,4)
    ;
    String name;
    int corePoolSize;
    int poolSize;
    ThreadPoolExecutor executor;

    ZmmudThreadPools(String name,int corePoolSize,int poolSize){
        this.name = name;
        this.corePoolSize = corePoolSize;
        this.poolSize = poolSize;
    }


    public void execute(Runnable task){
        this.executor.execute(task);
    }

    private static Logger logger = LogManager.getLogger(ZmmudThreadPools.class);
    static{
        ZmmudThreadPools pools[] = ZmmudThreadPools.values();

        for(int i = 0 ; i < pools.length ; i++){
            ZmmudThreadPools pool = pools[i];
            logger.info("Init Thread:" + pool.name);
            pool.executor  = new ThreadPoolExecutor(
                                    pool.corePoolSize, pool.poolSize, 60L, TimeUnit.SECONDS,
                                    new LinkedBlockingQueue<>(1024),
                                    r -> {
                                        Thread t = new Thread(r, pool.name + "-thread");
                                        t.setDaemon(true); // 强烈建议：客户端退出时，这些线程会自动销毁
                                        return t;
                                    },
                                    new ThreadPoolExecutor.CallerRunsPolicy()
                            );
            pool.executor.allowCoreThreadTimeOut(true);
        }

    }

}

package zm.mud.network.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ZmmudThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(ZmmudThread.class);
    private volatile boolean running = true;

    private Thread workerThread;

     @Override
    public final void run() {
        workerThread = Thread.currentThread();
        logger.info(this.getClass().getSimpleName() + " Start.");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
               this.doRun();
            } catch (Exception e) {
                logger.error("Failed to process inbound message", e);
            }
        }
    }


   public final void shutdown(){
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    protected abstract void doRun();
}

package zm.mud.network.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.IShutdownFunc;

public abstract class IZmmudThread implements Runnable ,IShutdownFunc{
    private static final Logger logger = LogManager.getLogger(IZmmudThread.class);
    private volatile boolean running = true;

    private Thread workerThread;

     @Override
    public final void run() {
        workerThread = Thread.currentThread();
        logger.info(this.getClass().getSimpleName() + " Start.");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                this.beforeLoop();
                boolean shouldContinue = this.doRun();
                if (!shouldContinue) {
                    break;
                }
            } catch (Exception e) {
                logger.error("Failed to process inbound message", e);
            }
        }
    }

   @Override
   public void shutdown(){
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    protected abstract boolean doRun();

    protected void beforeLoop(){}
}

package zm.mud.queue;

import java.util.concurrent.BlockingQueue;

public class InbByteMudGameMsgQueue {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(InbByteMudGameMsgQueue.class);

    private BlockingQueue<Integer> msgQueue;

    public InbByteMudGameMsgQueue(){
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    public void putByte(int b) {
        try {
            msgQueue.put(b);
        } catch (InterruptedException e) {
            logger.error("Failed to put byte into queue", e);
        }
    }

    public Integer takeByte() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Failed to take byte from queue", e);
            return null;
        }
    }

}

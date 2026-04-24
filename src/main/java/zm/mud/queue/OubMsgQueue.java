package zm.mud.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.outbound.message.OubMessage;

@Service
public class OubMsgQueue {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(OubMsgQueue.class);
    private final BlockingQueue<OubMessage> msgQueue;

    public OubMsgQueue() {
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    public void putMessage(OubMessage msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Error occurred while putting message in queue", e);
            Thread.currentThread().interrupt();
        }
    }

    public OubMessage takeMessage() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Error occurred while taking message from queue", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

  
}

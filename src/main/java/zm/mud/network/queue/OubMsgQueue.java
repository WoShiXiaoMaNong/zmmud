package zm.mud.network.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.network.outbound.message.OubMsg;

@Service
public class OubMsgQueue implements IZmmudQueue<OubMsg> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(OubMsgQueue.class);
    private final BlockingQueue<OubMsg> msgQueue;

    public OubMsgQueue() {
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public void put(OubMsg msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Error occurred while putting message in queue", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public OubMsg take() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Error occurred while taking message from queue", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

}

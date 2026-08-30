package zm.mud.core.network.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.session.MudSession;

@Service
public class OubMsgQueue implements IZmmudQueue<OubMsg> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(OubMsgQueue.class);
    private final BlockingQueue<OubMsg> msgQueue;

    public OubMsgQueue() {
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public void put(MudSession session,OubMsg msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Error occurred while putting message in queue", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public OubMsg take(MudSession session) {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Error occurred while taking message from queue", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

}

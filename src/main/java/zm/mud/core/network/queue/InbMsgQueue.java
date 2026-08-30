package zm.mud.core.network.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.session.MudSession;

@Service
public class InbMsgQueue implements IZmmudQueue<InbMsg> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbMsgQueue.class);

    private BlockingQueue<InbMsg> msgQueue;

    public InbMsgQueue() {
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public void put(MudSession session,InbMsg msg) {
        try {
            msg.setSession(session);
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Failed to put message into queue", e);
        }
    }

    @Override
    public InbMsg take(MudSession session) {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Failed to take message from queue", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

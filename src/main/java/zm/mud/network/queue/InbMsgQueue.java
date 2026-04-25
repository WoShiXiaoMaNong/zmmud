package zm.mud.network.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMessage;

@Service
public class InbMsgQueue implements IZmmudQueue<InbMessage> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbMsgQueue.class);

    private BlockingQueue<InbMessage> msgQueue;

    public InbMsgQueue() {
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public void put(InbMessage msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Failed to put message into queue", e);
        }
    }

    @Override
    public InbMessage take() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Failed to take message from queue", e);
            return null;
        }
    }
}

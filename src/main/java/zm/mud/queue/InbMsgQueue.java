package zm.mud.queue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.inbound.message.InbMessage;

@Service
public class InbMsgQueue {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(InbMsgQueue.class);

    private BlockingQueue<InbMessage> msgQueue;


    public InbMsgQueue(){
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }


    public void putMessage(InbMessage msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Failed to put message into queue", e);
        }
    }

    public InbMessage takeMessage() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Failed to take message from queue", e);
            return null;
        }
    }
}

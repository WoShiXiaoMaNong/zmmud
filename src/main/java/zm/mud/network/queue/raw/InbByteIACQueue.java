package zm.mud.network.queue.raw;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;

import zm.mud.network.queue.IZmmudQueue;


@Service
public class InbByteIACQueue implements IZmmudQueue<Integer> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(InbByteIACQueue.class);

    private BlockingQueue<Integer> msgQueue;

    public InbByteIACQueue(){
        this.msgQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public void put(Integer b) {
        try {
            msgQueue.put(b);
        } catch (InterruptedException e) {
            logger.error("Failed to put byte into queue", e);
        }
    }

    @Override
    public Integer take() {
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            logger.error("Failed to take byte from queue", e);
            return null;
        }
    }

}

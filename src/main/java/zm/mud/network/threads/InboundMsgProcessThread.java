package zm.mud.network.threads;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.processor.InbMsgProcessor;
import zm.mud.network.queue.InbMsgQueue;

@Service
public class InboundMsgProcessThread implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InboundMsgProcessThread.class);

    @Autowired
    private InbMsgQueue msgQueue;

    @Autowired
    private List<InbMsgProcessor> printProcessor;

    private volatile boolean running = true;

    private Thread workerThread;

    @Override
    public void shutdown() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                InbMessage msg = msgQueue.take();
                for (InbMsgProcessor inbMsgProcessor : printProcessor) {
                    boolean shouldContinue = inbMsgProcessor.processMessage(msg);
                    if (!shouldContinue) {
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Failed to process inbound message", e);
            }
        }
    }

}

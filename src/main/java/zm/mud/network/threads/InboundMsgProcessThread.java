package zm.mud.network.threads;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.processor.InbMsgProcessor;
import zm.mud.network.queue.InbMsgQueue;

@Service
public class InboundMsgProcessThread extends ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InboundMsgProcessThread.class);

    @Autowired
    private InbMsgQueue msgQueue;

    @Autowired
    private List<InbMsgProcessor> printProcessor;

    @Override
    public void doRun() {
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
            throw e;
        }
    }

}

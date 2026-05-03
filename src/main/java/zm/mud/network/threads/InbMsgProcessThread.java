package zm.mud.network.threads;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMsg;
import zm.mud.network.inbound.processor.IInbMsgProcessor;
import zm.mud.network.queue.InbMsgQueue;

@Service
public class InbMsgProcessThread extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbMsgProcessThread.class);

    @Autowired
    private InbMsgQueue msgQueue;

    @Autowired
    private List<IInbMsgProcessor> printProcessor;

    @Override
    public boolean doRun() {
        try {
            InbMsg msg = msgQueue.take();
            for (IInbMsgProcessor inbMsgProcessor : printProcessor) {
                boolean shouldContinue = inbMsgProcessor.processMessage(msg);
                if (!shouldContinue) {
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to process inbound message", e);
            throw e;
        }
    }

}

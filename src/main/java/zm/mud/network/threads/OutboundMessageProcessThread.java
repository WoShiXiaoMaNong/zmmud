package zm.mud.network.threads;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.outbound.processor.OubMsgProcessor;
import zm.mud.network.queue.OubMsgQueue;

@Service
public class OutboundMessageProcessThread extends ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(OutboundMessageProcessThread.class);

    @Autowired
    private OubMsgQueue oubMsgQueue;

    @Autowired
    private List<OubMsgProcessor> oubMsgProcessors;

    @Override
    public void doRun() {
        try {
            zm.mud.network.outbound.message.OubMessage msg = oubMsgQueue.take();
            for (OubMsgProcessor processor : oubMsgProcessors) {
                if (processor.processMessage(msg)) {
                    break; // Message processed, move to next message
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred in OutboundMessageProcessThread", e);
            throw e;
        }
    }

}

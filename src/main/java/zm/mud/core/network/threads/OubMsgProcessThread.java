package zm.mud.core.network.threads;

import java.util.List;

import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.network.outbound.processor.IOubMsgProcessor;
import zm.mud.core.network.queue.OubMsgQueue;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;


public class OubMsgProcessThread extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(OubMsgProcessThread.class);


    private OubMsgQueue oubMsgQueue;


    private List<IOubMsgProcessor> oubMsgProcessors;

    public OubMsgProcessThread(MudSession session){
        super(session);
        this.oubMsgQueue = SpringBeanUtil.getBean(OubMsgQueue.class);
        this.oubMsgProcessors = SpringBeanUtil.getAllBeansByType(IOubMsgProcessor.class);
    }
    @Override
    public boolean doRun() {
        try {
            OubMsg msg = oubMsgQueue.take(this.getSession());
            for (IOubMsgProcessor processor : oubMsgProcessors) {
                if (processor.processMessage(msg)) {
                    break; // Message processed, move to next message
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error occurred in OutboundMessageProcessThread", e);
            return true;
        }
    }

}

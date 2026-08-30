package zm.mud.core.network.threads;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.inbound.processor.IInbMsgProcessor;
import zm.mud.core.network.queue.InbMsgQueue;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;


public class InbMsgProcessThread extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbMsgProcessThread.class);

    @Autowired
    private InbMsgQueue msgQueue;

    @Autowired
    private List<IInbMsgProcessor> inbProcessor;

    public InbMsgProcessThread(MudSession session){
        super(session);
        this.msgQueue = SpringBeanUtil.getBean(InbMsgQueue.class);
        this.inbProcessor = SpringBeanUtil.getAllBeansByType(IInbMsgProcessor.class);
    }

    @Override
    public boolean doRun() {
        try {
            InbMsg msg = msgQueue.take(this.getSession());
            for (IInbMsgProcessor inbMsgProcessor : inbProcessor) {
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

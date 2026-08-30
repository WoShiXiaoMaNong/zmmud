package zm.mud.core.api;

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.inbound.processor.MsgHandlerProcessor;
import zm.mud.core.session.MudSession;
import zm.mud.core.network.inbound.processor.InbTriggerProcessor;

@Service
public class InbMsgService {
    private static final Logger logger = LogManager.getLogger(InbMsgService.class);

    @Autowired
    private MsgHandlerProcessor msgHandlerProcessor;

    @Autowired
    private InbTriggerProcessor triggerProcessor;

    public void registerMsgHandler(MudSession session,Function<InbMsg,Boolean> handler){
        this.msgHandlerProcessor.register(session,handler);
    }

    public void registerTrigger(MudSession session,Trigger trigger){
        if( trigger == null ){
            logger.warn("Trigger is null. Skip!");
            return;
        }
        triggerProcessor.register(session, trigger);
    }
}

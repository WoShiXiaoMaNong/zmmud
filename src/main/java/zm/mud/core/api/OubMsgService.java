package zm.mud.core.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.network.outbound.message.NrmOubMsg;
import zm.mud.core.network.outbound.processor.OubTriggerProcessor;
import zm.mud.core.network.queue.OubMsgQueue;

@Service
public class OubMsgService {
    private static final Logger logger = LogManager.getLogger(OubMsgService.class);

    @Autowired
    private OubMsgQueue oubMsgQueue;

    @Autowired
    private OubTriggerProcessor triggerProcessor;

    public void send(String msg){

        this.oubMsgQueue.put(new NrmOubMsg(msg));
    }

    public void registerTrigger(Trigger trigger){
        if( trigger == null ){
            logger.warn("Trigger is null. Skip!");
            return;
        }
        triggerProcessor.register(trigger);
    }
}

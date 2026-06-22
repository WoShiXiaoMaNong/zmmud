package zm.mud.core.trigger;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.api.InbMsgService;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.trigger.cfg.TriggerType;

@Service
public class TriggerRegister {
    private static Logger logger = LogManager.getLogger(TriggerRegister.class);
    
    @Autowired
    private InbMsgService inbMsgService;

    @Autowired
    private OubMsgService oubMsgService;

 
    public void registerTrigger(Trigger trigger){
        if( trigger.getTriggerType() == null){
            logger.error("Register Trigger error: The trigger type is null. Trigger Name: " + trigger.getTriggerName());
            return;
        }

        if( trigger.getTriggerType().equals(TriggerType.INBOUNG_TRIGGER)){
            this.inbMsgService.registerTrigger(trigger);
        }else if( trigger.getTriggerType().equals(TriggerType.OUTBOUNG_TRIGGER)){
            this.oubMsgService.registerTrigger(trigger);
        }else{
            logger.error("Unsupport Trigger Type" + trigger.getTriggerType() +  ": Trigger Name: " + trigger.getTriggerName());
            return;
        }
    }




}

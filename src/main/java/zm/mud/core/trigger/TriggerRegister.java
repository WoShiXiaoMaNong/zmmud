package zm.mud.core.trigger;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import zm.mud.core.api.InbMsgService;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.trigger.action.TestAction;
import zm.mud.core.trigger.cfg.TiggerType;
import zm.mud.core.trigger.matcher.TestMatcher;

@Service
public class TriggerRegister {
    private static Logger logger = LogManager.getLogger(TriggerRegister.class);
    
    @Autowired
    private InbMsgService inbMsgService;

    @Autowired
    private OubMsgService oubMsgService;

    @PostConstruct
    public void registerTriggers(){
        List<Trigger> triggers = this.loadTriggers();
        for(Trigger trigger : triggers){
            this.registerTrigger(trigger);
        }
    }

    public void registerTrigger(Trigger trigger){
        if( trigger.getTriggerType() == null){
            logger.error("Register Trigger error: The trigger type is null. Trigger Name: " + trigger.getTriggerName());
            return;
        }

        if( trigger.getTriggerType().equals(TiggerType.INBOUNG_TRIGGER)){
            this.inbMsgService.registerTrigger(trigger);
        }else if( trigger.getTriggerType().equals(TiggerType.OUTBOUNG_TRIGGER)){
            this.oubMsgService.registerTrigger(trigger);
        }else{
            logger.error("Unsupport Trigger Type" + trigger.getTriggerType() +  ": Trigger Name: " + trigger.getTriggerName());
            return;
        }
    }



    private List<Trigger> loadTriggers(){
        Trigger testTrigger = new Trigger(TiggerType.OUTBOUNG_TRIGGER,"TEST Trigger",new TestMatcher(), new TestAction());

        List<Trigger> triggers =  new ArrayList<>();
        triggers.add(testTrigger);
        return triggers;
    }
}

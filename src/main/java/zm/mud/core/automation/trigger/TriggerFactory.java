package zm.mud.core.automation.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import zm.mud.core.automation.action.IAction;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerType;
import zm.mud.core.automation.trigger.matcher.IMatcher;

@Service
public class TriggerFactory {
    private static final Logger logger = LogManager.getLogger(TriggerFactory.class);
    
    @Autowired
    private TriggerLoader triggerLoader;

    @Autowired
    private TriggerRegister triggerRegister;

    @Autowired
    private ApplicationContext ctx;

    private List<TriggerConfigEntry> triggers;
    private Map<String,TriggerConfigEntry> triggerMap;


    public TriggerFactory(){

    }

    public Trigger buildByeName(String triggerName){
        return this.build(this.triggerMap.get(triggerName));
    }
   

    public Trigger build(TriggerConfigEntry cfgEntry){
        String trggerName = cfgEntry.getName();
        String triggerTypeStr = cfgEntry.getType();
        MatcherAndActionConfigEntry actionEntry = cfgEntry.getAction();
        MatcherAndActionConfigEntry matcherEntry = cfgEntry.getMatcher();
        Integer remainningCount = cfgEntry.getRemainningCount();


        IMatcher matcher = (IMatcher) this.ctx.getBean("MATCHER_" + matcherEntry.getType());
        matcher.setExpression(matcherEntry.getExpression());

        IAction action = (IAction) this.ctx.getBean("ACTION_" + actionEntry.getType());
        action.setExpression(actionEntry.getExpression());
        action.setParams(cfgEntry.getAction().getParams());

       TriggerType triggerType = null;
       if("inbound".equalsIgnoreCase(triggerTypeStr)){
            triggerType = TriggerType.INBOUNG_TRIGGER;
       }else if("outbound".equalsIgnoreCase(triggerTypeStr)){
            triggerType = TriggerType.OUTBOUNG_TRIGGER;
       }else{
            logger.error("unsupport trigger type: " + triggerTypeStr);
            return null;
       }


        Trigger trigger = new Trigger(triggerType,trggerName,matcher, action,remainningCount);
        trigger.setSync(cfgEntry.isSync());
        trigger.setUnique(cfgEntry.isUnique());
        trigger.setAutoRegister(cfgEntry.isAutoRegister());
        return trigger;

    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationReady() {
        logger.info("Trigger init start....");
        this.triggers = this.triggerLoader.loadTriggers();
        this.triggerMap = new HashMap<>();
        for(TriggerConfigEntry cfgEntry : this.triggers){
            this.triggerMap.put(cfgEntry.getName(),cfgEntry);
        }
        logger.info("Trigger init finished");

        logger.info("Register triggers");
        for(TriggerConfigEntry cfgEntry : this.triggers){
            if( !cfgEntry.isAutoRegister()){
                logger.debug("[Skip]Not an Auto-Register Trigger :" + cfgEntry.getName());
                continue;
            }
            Trigger trgger = this.build(cfgEntry);
            this.triggerRegister.registerTrigger(trgger);
        }
    }



    public List<TriggerConfigEntry> getTriggers() {
        return triggers;
    }

    
}

package zm.mud.core.automation.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.TypeReference;

import zm.mud.core.automation.action.IAction;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerType;
import zm.mud.core.automation.trigger.matcher.IMatcher;
import zm.mud.core.cfg.CustomCfgLoader;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;


@Service
public class TriggerFactory {
    private static final Logger logger = LogManager.getLogger(TriggerFactory.class);
    
    private Map<String/* Session ID */,List<TriggerConfigEntry>> triggers;
    private Map<String/* Session ID */,Map<String,TriggerConfigEntry>> triggerMap;



    public TriggerFactory(){
        this.triggers = new ConcurrentHashMap<>();
        this.triggerMap = new ConcurrentHashMap<>();
    }

    public Trigger buildByeName(MudSession session, String triggerName){
        Map<String,TriggerConfigEntry> trggerMapForCurrentSession = this.triggerMap.get(session.getSessionId());
        if(trggerMapForCurrentSession == null){
            return null;
        }
        return this.build(session,trggerMapForCurrentSession.get(triggerName));
    }
   

    public Trigger build(MudSession session,TriggerConfigEntry cfgEntry){
        String trggerName = cfgEntry.getName();
        String triggerTypeStr = cfgEntry.getType();
        MatcherAndActionConfigEntry actionEntry = cfgEntry.getAction();
        MatcherAndActionConfigEntry matcherEntry = cfgEntry.getMatcher();
        Integer remainningCount = cfgEntry.getRemainningCount();


        IMatcher matcher =  SpringBeanUtil.getBean("MATCHER_" + matcherEntry.getType(),IMatcher.class);
        matcher.setExpression(matcherEntry.getExpression());

        IAction action = SpringBeanUtil.getBean("ACTION_" + actionEntry.getType(),IAction.class);
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


        Trigger trigger = new Trigger(session,triggerType,trggerName,matcher, action,remainningCount);
        trigger.setSync(cfgEntry.isSync());
        trigger.setUnique(cfgEntry.isUnique());
        trigger.setAutoRegister(cfgEntry.isAutoRegister());
        return trigger;

    }

    public void load(MudSession session) {
        logger.info("Trigger init start....");
        TriggerRegister triggerRegister = SpringBeanUtil.getBean(TriggerRegister.class);
        List<TriggerConfigEntry> triggersForCurrentSession = (List<TriggerConfigEntry> ) CustomCfgLoader.loadUIConfig("pkuxkx", "triggers",
                    new TypeReference<List<TriggerConfigEntry>>(){});
        this.triggers.put(session.getSessionId(),triggersForCurrentSession);
     
        for(TriggerConfigEntry cfgEntry : triggersForCurrentSession){
            Map<String,TriggerConfigEntry> triggerMapForCurentSession = this.triggerMap.get(session.getSessionId());
            if( triggerMapForCurentSession == null){
                triggerMapForCurentSession = new HashMap<>();
                this.triggerMap.put(session.getSessionId(),triggerMapForCurentSession);
            }
            triggerMapForCurentSession.put(cfgEntry.getName(),cfgEntry);
        }
        logger.info("Trigger init finished");

        logger.info("Register triggers");
        for(TriggerConfigEntry cfgEntry : triggersForCurrentSession){
            if( !cfgEntry.isAutoRegister()){
                logger.debug("[Skip]Not an Auto-Register Trigger :" + cfgEntry.getName());
                continue;
            }
            Trigger trgger = this.build(session,cfgEntry);
            triggerRegister.registerTrigger(session,trgger);
        }
    }

    public List<TriggerConfigEntry> getTriggers(MudSession session) {
        return triggers.get(session.getSessionId());
    }

    
}

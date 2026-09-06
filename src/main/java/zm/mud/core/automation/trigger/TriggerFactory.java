package zm.mud.core.automation.trigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.TypeReference;
import jakarta.annotation.PostConstruct;
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

    private Map<String/* Mud World Code */,List<TriggerConfigEntry>> worldTriggers;

    private Map<String/* Session ID */,List<TriggerConfigEntry>> sessionTriggers;
    private Map<String/* Session ID */,Map<String,TriggerConfigEntry>> sessionTriggerMap;

    @Autowired
    private TriggerRegister triggerRegister;

    public TriggerFactory(){
        this.worldTriggers = new ConcurrentHashMap<>();
        this.sessionTriggers = new ConcurrentHashMap<>();
        this.sessionTriggerMap = new ConcurrentHashMap<>();
    }

    public synchronized Trigger buildByeName(MudSession session, String triggerName){
        Map<String,TriggerConfigEntry> trggerMapForCurrentSession = this.sessionTriggerMap.get(session.getSessionId());
        if(trggerMapForCurrentSession == null){
            return null;
        }
        return this.build(session,trggerMapForCurrentSession.get(triggerName));
    }
   

    public synchronized Trigger build(MudSession session,TriggerConfigEntry cfgEntry){
        String trggerName = cfgEntry.getName();
        String triggerTypeStr = cfgEntry.getType();
        MatcherAndActionConfigEntry actionEntry = cfgEntry.getAction();
        MatcherAndActionConfigEntry matcherEntry = cfgEntry.getMatcher();
        Integer remainingCount = cfgEntry.getRemainingCount();


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


        Trigger trigger = new Trigger(session,triggerType,trggerName,matcher, action,remainingCount);
        trigger.setSync(cfgEntry.isSync());
        trigger.setUnique(cfgEntry.isUnique());
        trigger.setAutoRegister(cfgEntry.isAutoRegister());
        return trigger;

    }

    public synchronized void reload(MudSession session) {
        
        logger.info("Trigger init start....");
        List<TriggerConfigEntry> triggersForCurrentWorld = (List<TriggerConfigEntry> ) CustomCfgLoader.loadUIConfig(session.getMudWorldCode(), "triggers",
                    new TypeReference<List<TriggerConfigEntry>>(){});
        this.worldTriggers.put(session.getMudWorldCode(),triggersForCurrentWorld);

        this.sessionTriggers.remove(session.getSessionId());
        this.sessionTriggerMap.remove(session.getSessionId());

        this.sessionTriggers.put(session.getSessionId(), triggersForCurrentWorld);
     
        for(TriggerConfigEntry cfgEntry : triggersForCurrentWorld){
            Map<String,TriggerConfigEntry> triggerMapForCurentSession = this.sessionTriggerMap.get(session.getSessionId());
            if( triggerMapForCurentSession == null){
                triggerMapForCurentSession = new HashMap<>();
                this.sessionTriggerMap.put(session.getSessionId(),triggerMapForCurentSession);
            }
            triggerMapForCurentSession.put(cfgEntry.getName(),cfgEntry);
        }
        logger.info("Trigger init finished");

        logger.info("Register triggers");
        for(TriggerConfigEntry cfgEntry :  triggersForCurrentWorld){
            if( !cfgEntry.isAutoRegister()){
                logger.debug("[Skip]Not an Auto-Register Trigger :" + cfgEntry.getName());
                continue;
            }
            Trigger trgger = this.build(session,cfgEntry);
            triggerRegister.registerTrigger(session,trgger);
        }
    }

    public List<TriggerConfigEntry> getTriggers(MudSession session) {
        return this.sessionTriggers.get(session.getSessionId());
    }

     public List<TriggerConfigEntry> getWorldTriggers(String mudWorldCode) {
        return this.worldTriggers.get(mudWorldCode);
    }


    public synchronized void save(String mudWorldCode, List<TriggerConfigEntry> configs) {

        // 1. 保存配置文件

        CustomCfgLoader.saveUIConfig(
                mudWorldCode,
                "triggers",
                configs
        );

        // 2. 重新加载

        for( String sessionId: sessionTriggers.keySet()){
            MudSession session = MudSession.getSession(sessionId);
            if(session == null){
                continue;
            }
            reload(session);
        }
       
    }
    
}

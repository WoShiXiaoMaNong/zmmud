package zm.mud.core.network.inbound.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.automation.trigger.cfg.MatchResult;
import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;

@Service
public class InbTriggerProcessor extends AbsSessionValidatingInbMsgProcessor {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbTriggerProcessor.class);

    private Lock lock;
    private Map<String /*SessionId*/,List<Trigger>> triggers;
    private Map<String /*SessionId*/,Map<String,Trigger>> triggerMap;

    public InbTriggerProcessor() {
        this.triggers = new HashMap<>();
        this.triggerMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    @Override
    protected boolean doProcess(InbMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return true;
        }
        this.lock.lock();
        try {
            MudSession session = msg.getSession();
            String sessionId = session.getSessionId();
            List<Trigger> triggerForCurrentSession = this.triggers.get(sessionId);
            if( triggerForCurrentSession == null){
                return true;
            }
            Iterator<Trigger> iterator = triggerForCurrentSession.iterator();
            while (iterator.hasNext()) {
                Trigger trigger = iterator.next();
                // 1. 检查调用前是否已死亡（例如被其他线程或之前的逻辑改变了状态）
                if (trigger.died()) {
                    iterator.remove(); // 安全删除
                    this.triggerMap.remove(trigger.getUniqueKey());
                    logger.debug(trigger.getTriggerName() + " : removed !");
                    continue;
                }
                try {
                    tryInvokeTrigger(trigger, msg);
                } catch (Exception e) {
                    logger.error("Trigger process error!", e);
                }
                
            }
           
        } finally {
            this.lock.unlock();
        }
        return true;
    }

    private void tryInvokeTrigger(Trigger trigger, InbMsg msg) {
        ZmmudThreadPools.MUD_TRRIGER.execute(
                () -> {
                    MatchResult ret = trigger.match(msg.getContent());
                    if (ret.isMatched()) {
                        trigger.fire(ret);
                    }
                });

    }

    public void register(MudSession session,Trigger trigger) {
        
        this.lock.lock();
        try {
            String sessionId = session.getSessionId();
            Map<String,Trigger> triggerMapForCurrentSession = this.triggerMap.get(sessionId);
            if(triggerMapForCurrentSession == null){
                triggerMapForCurrentSession = new HashMap<>();
                this.triggerMap.put(sessionId,triggerMapForCurrentSession);
            }
            Trigger originalTrigger = triggerMapForCurrentSession.get(trigger.getUniqueKey());
            
            boolean triggerExisting = originalTrigger!=null && !originalTrigger.died();
            if(trigger.isUnique() && triggerExisting){
                logger.debug("[Skip] Unique trigger already existed:" + trigger.getUniqueKey());
                return;
            }

            List<Trigger> triggerForCurrentSession = this.triggers.get(sessionId);
            if( triggerForCurrentSession == null){
                triggerForCurrentSession = new ArrayList<>();
                this.triggers.put(sessionId,triggerForCurrentSession);
            }
            triggerForCurrentSession.add(trigger);
            triggerMapForCurrentSession.put(trigger.getUniqueKey(), trigger);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }

}

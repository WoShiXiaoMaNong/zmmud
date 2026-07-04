package zm.mud.core.network.inbound.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;

@Service
public class InbTriggerProcessor implements IInbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbTriggerProcessor.class);

    private Lock lock;
    private List<Trigger> triggers;
    private Map<String,Trigger> triggerMap;

    public InbTriggerProcessor() {
        this.triggers = new ArrayList<>();
        this.triggerMap = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    @Override
    public boolean processMessage(InbMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return true;
        }
        this.lock.lock();
        try {
            Iterator<Trigger> iterator = this.triggers.iterator();
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

    public void register(Trigger trigger) {
        
        this.lock.lock();
        try {
            Trigger originalTrigger = this.triggerMap.get(trigger.getUniqueKey());
            
            boolean triggerExisting = originalTrigger!=null && !originalTrigger.died();
            if(trigger.isUnique() && triggerExisting){
                logger.debug("[Skip] Unique trigger already existed:" + trigger.getUniqueKey());
                return;
            }
            this.triggers.add(trigger);
            this.triggerMap.put(trigger.getUniqueKey(), trigger);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }

}

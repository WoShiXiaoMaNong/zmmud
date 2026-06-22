package zm.mud.core.network.outbound.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.core.trigger.matcher.IMatcher;

@Service
public class OubTriggerProcessor implements IOubMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(OubTriggerProcessor.class);

    private Lock lock;
    private List<Trigger> triggers;

    public OubTriggerProcessor(){
        this.triggers = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    @Override
    public boolean processMessage(OubMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return false;
        }
        this.lock.lock();
       try {
            Iterator<Trigger> iterator = this.triggers.iterator();
            while (iterator.hasNext()) {
                Trigger trigger = iterator.next();
                // 1. 检查调用前是否已死亡（例如被其他线程或之前的逻辑改变了状态）
                if (trigger.died()) {
                    iterator.remove(); // 安全删除
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
        return false;
    }

   private void tryInvokeTrigger(Trigger trigger, OubMsg msg) {
        ZmmudThreadPools.MUD_TRRIGER.execute(
                () -> {
                    MatchResult ret = trigger.match(msg.getContent());
                    if (ret.isMatched()) {
                        trigger.fire(ret);
                    }
                });

    }

    public void register(Trigger trigger){
        this.lock.lock();
        try{
            this.triggers.add(trigger);
        }finally{
            this.lock.unlock();
        }
    }
    @Override
    public int getOrder() {
        return 2;
    }

  
}

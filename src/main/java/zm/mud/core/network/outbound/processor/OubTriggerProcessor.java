package zm.mud.core.network.outbound.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
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
            ZmmudThreadPools.MUD_TRRIGER.execute(
                    () -> {
                        invokeTrigger(msg);
                    });
        } finally {
            this.lock.unlock();
        }
        return false;
    }

    private void invokeTrigger(OubMsg msg) {
        for (Trigger trigger : this.triggers) {
            try {
                IMatcher matcher = trigger.getMatcher();
                MatchResult ret = matcher.match(msg);
                if (ret.isMatched()) {
                    trigger.getAction().execute(trigger, ret);
                }

            } catch (Exception e) {
                logger.error("Trigger process error!", e);
            }
        }
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

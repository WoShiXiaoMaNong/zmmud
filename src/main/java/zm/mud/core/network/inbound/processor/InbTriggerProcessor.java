package zm.mud.core.network.inbound.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.core.trigger.matcher.IMatcher;

@Service
public class InbTriggerProcessor implements IInbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbTriggerProcessor.class);

    private Lock lock;
    private List<Trigger> triggers;

    public InbTriggerProcessor(){
        this.triggers = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    @Override
    public boolean processMessage(InbMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return true;
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
        return true;
    }

    private void invokeTrigger(InbMsg msg) {
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
        return 3;
    }

  
}

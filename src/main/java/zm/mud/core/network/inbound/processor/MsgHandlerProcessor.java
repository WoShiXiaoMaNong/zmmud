package zm.mud.core.network.inbound.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;

@Service
public class MsgHandlerProcessor implements IInbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MsgHandlerProcessor.class);


    private List<Function<InbMsg,Boolean>> handlers;

    public MsgHandlerProcessor(){
        this.handlers = new ArrayList<>();
    }

    @Override
    public boolean processMessage(InbMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return true;
        }
        for(Function<InbMsg,Boolean> handler : this.handlers){
            try{
                handler.apply(msg);
            }catch(Exception e){
                logger.error("handing msg error!",e);
            }
        }
        return true;
    }

    public void register(Function<InbMsg,Boolean> handler){
        this.handlers.add(handler);
    }

    @Override
    public int getOrder() {
        return 1;
    }

  
}

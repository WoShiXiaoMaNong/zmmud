package zm.mud.core.network.inbound.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.session.MudSession;

@Service
public class MsgHandlerProcessor implements IInbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MsgHandlerProcessor.class);


    private Map<String/*Session ID*/,List<Function<InbMsg,Boolean>>> handlers;

    public MsgHandlerProcessor(){
        this.handlers = new HashMap<>();
    }
    
    @Override
    public boolean processMessage(InbMsg msg) {
        if (msg instanceof IACConfirmInbMsg) {
            return true;
        }
        MudSession session = msg.getSession();
        String sessionId = session.getSessionId();
        List<Function<InbMsg,Boolean>> handlersForCurrentSession = handlers.get(sessionId);
        if(handlersForCurrentSession == null){
            return true;
        }
        for(Function<InbMsg,Boolean> handler : handlersForCurrentSession){
            try{
                handler.apply(msg);
            }catch(Exception e){
                logger.error("handing msg error!",e);
            }
        }
        return true;
    }

    public void register(MudSession session,Function<InbMsg,Boolean> handler){
        String sessionId = session.getSessionId();
        List<Function<InbMsg,Boolean>> handlersForCurrentSession = handlers.get(sessionId);
        if(handlersForCurrentSession == null){
            handlersForCurrentSession = new ArrayList<>();
            handlers.put(sessionId,handlersForCurrentSession);
        }
        handlersForCurrentSession.add(handler);
    }

    @Override
    public int getOrder() {
        return 1;
    }

  
}

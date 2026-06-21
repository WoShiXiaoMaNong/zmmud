package zm.mud.api;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMsg;
import zm.mud.network.inbound.processor.MsgHandlerProcessor;

@Service
public class InbMsgService {
    
    @Autowired
    private MsgHandlerProcessor msgHandlerProcessor;
   
    public void registerMsgHandler(Function<InbMsg,Boolean> handler){
        this.msgHandlerProcessor.register(handler);
    }
}

package zm.mud.network.inbound.processor;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.IACConfirmInbMsg;
import zm.mud.network.inbound.message.InbMsg;

@Service
public class PrintProcessor implements IInbMsgProcessor,Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(PrintProcessor.class);
    @Override
    public boolean processMessage(InbMsg msg) {
        if( msg instanceof IACConfirmInbMsg){
            return true;
        }
        System.out.println(msg);
        return true;
    }
    @Override
    public int getOrder() {
        return 1;
    }

}

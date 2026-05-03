package zm.mud.ui.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.IACConfirmInbMsg;
import zm.mud.network.inbound.message.InbMsg;
import zm.mud.network.inbound.processor.IInbMsgProcessor;
import zm.mud.ui.ZmMudUI;

@Service
public class PrintProcessor implements IInbMsgProcessor,Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(PrintProcessor.class);

    @Autowired
    private ZmMudUI mainScreen;
    @Override
    public boolean processMessage(InbMsg msg) {
        if( msg instanceof IACConfirmInbMsg){
            return true;
        }
        mainScreen.printlnToScreen(msg.getContent());
        //System.out.println(msg);
        return true;
    }
    @Override
    public int getOrder() {
        return 1;
    }

}

package zm.mud.inbound.processor;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.inbound.message.InbMessage;

@Service
public class PrintProcessor implements InbMsgProcessor,Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(PrintProcessor.class);
    @Override
    public boolean processMessage(InbMessage msg) {
        System.out.println(msg);
        return true;
    }
    @Override
    public int getOrder() {
        return 1;
    }

}

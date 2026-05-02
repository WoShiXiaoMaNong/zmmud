package zm.mud.network.outbound.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.network.outbound.message.OubMsg;
import zm.mud.network.outbound.sender.OubMsgSender;

@Service
public class OubSendProcessor implements IOubMsgProcessor,Ordered {

    @Autowired
    private OubMsgSender oubMessageSender;

    @Override
    public boolean processMessage(OubMsg msg) {
        oubMessageSender.send(msg);
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}

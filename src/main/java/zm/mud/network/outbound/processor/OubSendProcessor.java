package zm.mud.network.outbound.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.network.outbound.message.OubMessage;
import zm.mud.network.outbound.sender.OubMessageSender;

@Service
public class OubSendProcessor implements OubMsgProcessor,Ordered {

    @Autowired
    private OubMessageSender oubMessageSender;

    @Override
    public boolean processMessage(OubMessage msg) {
        oubMessageSender.send(msg);
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}

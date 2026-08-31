package zm.mud.core.network.outbound.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.network.outbound.sender.OubMsgSender;

@Service
public class OubSendProcessor extends AbSessionValidatingOubMsgProcessor {

    @Autowired
    private OubMsgSender oubMessageSender;

    @Override
    protected boolean doProcess(OubMsg msg) {
        oubMessageSender.send(msg);
        return false;
    }

    @Override
    public int getOrder() {
        return 3;
    }

}

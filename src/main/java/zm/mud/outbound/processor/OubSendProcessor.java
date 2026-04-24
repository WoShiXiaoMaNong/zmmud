package zm.mud.outbound.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.outbound.message.OubMessage;

@Service
public class OubSendProcessor implements OubMsgProcessor,Ordered {

    @Autowired
    private MudClient mudClient;

    @Override
    public boolean processMessage(OubMessage msg) {
        mudClient.sendLine(msg);
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}

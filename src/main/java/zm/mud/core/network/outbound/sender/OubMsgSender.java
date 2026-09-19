package zm.mud.core.network.outbound.sender;

import org.springframework.stereotype.Service;

import zm.mud.core.client.MudClient;
import zm.mud.core.network.outbound.message.OubMsg;

@Service
public class OubMsgSender {


    public void send(OubMsg msg){
        MudClient client = msg.getSession().getClient();
        client.sendLine(msg);
    }
}

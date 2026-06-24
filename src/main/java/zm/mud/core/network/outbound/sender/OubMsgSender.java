package zm.mud.core.network.outbound.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.client.MudClient;
import zm.mud.core.network.outbound.message.OubMsg;

@Service
public class OubMsgSender {

    @Autowired
    private MudClient mudClient;

    public void send(OubMsg msg){
         mudClient.sendLine(msg);
    }
}

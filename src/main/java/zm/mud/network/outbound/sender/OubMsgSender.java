package zm.mud.network.outbound.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.outbound.message.OubMsg;

@Service
public class OubMsgSender {

    @Autowired
    private MudClient mudClient;

    public void send(OubMsg msg){
         mudClient.sendLine(msg);
    }
}

package zm.mud.network.outbound.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.outbound.message.OubMessage;

@Service
public class OubMessageSender {

    @Autowired
    private MudClient mudClient;

    public void send(OubMessage msg){
         mudClient.sendLine(msg);
    }
}

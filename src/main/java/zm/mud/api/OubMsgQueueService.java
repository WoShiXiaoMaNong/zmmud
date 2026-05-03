package zm.mud.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.outbound.message.NrmOubMsg;
import zm.mud.network.queue.OubMsgQueue;

@Service
public class OubMsgQueueService {

    @Autowired
    private OubMsgQueue oubMsgQueue;

    public void send(String msg){

        this.oubMsgQueue.put(new NrmOubMsg(msg));
    }
}

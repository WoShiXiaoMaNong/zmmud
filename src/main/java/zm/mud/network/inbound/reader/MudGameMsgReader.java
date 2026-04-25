package zm.mud.network.inbound.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.message.NormalInbMsg;
import zm.mud.network.queue.ZmmudQueue;

@Service
public class MudGameMsgReader implements InbMessageReader<Integer> {

    @Autowired
    private MudClient client;

    @Override
    public InbMessage readInbMessage(int firstByte, ZmmudQueue<Integer> iacByteQueue) {
        List<Integer> byteList = new ArrayList<>();
        byteList.add(firstByte);

        // read until \r\n
        int ch;
        while ((ch = iacByteQueue.take()) != -1) {
            if (ch == '\r') {
                ch = iacByteQueue.take();
                if (ch == '\n') {
                    break;
                }
            }
            byteList.add(ch);
        }

        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i).byteValue();
        }
        return new NormalInbMsg(new String(bytes, client.getCharset()));
    }

}

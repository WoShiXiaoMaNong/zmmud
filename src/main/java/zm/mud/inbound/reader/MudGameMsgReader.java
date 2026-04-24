package zm.mud.inbound.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.inbound.message.InbMessage;
import zm.mud.inbound.message.NormalInbMsg;

@Service
public class MudGameMsgReader implements InbMessageReader {

    @Override
    public InbMessage readInbMessage(int firstByte, MudClient client) {
        List<Integer> byteList = new ArrayList<>();
        byteList.add(firstByte);

        // read until \r\n
        int ch;
        while ((ch = client.read()) != -1) {
            if (ch == '\r') {
                ch = client.read();
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

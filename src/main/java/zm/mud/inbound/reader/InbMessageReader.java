package zm.mud.inbound.reader;

import zm.mud.client.MudClient;
import zm.mud.inbound.message.InbMessage;

public interface InbMessageReader { 
     InbMessage readInbMessage(int firstByte,MudClient client);
}

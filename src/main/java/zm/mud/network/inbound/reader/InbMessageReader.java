package zm.mud.network.inbound.reader;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.queue.ZmmudQueue;

public interface InbMessageReader<T> { 
     InbMessage readInbMessage(int firstByte,ZmmudQueue<T> iacByteQueue);
     
}

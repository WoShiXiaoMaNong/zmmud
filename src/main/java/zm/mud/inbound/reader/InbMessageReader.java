package zm.mud.inbound.reader;

import zm.mud.inbound.message.InbMessage;
import zm.mud.queue.ZmmudQueue;

public interface InbMessageReader<T> { 
     InbMessage readInbMessage(int firstByte,ZmmudQueue<T> iacByteQueue);
     
}

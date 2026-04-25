package zm.mud.network.inbound.reader;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.queue.IZmmudQueue;

public interface InbMessageReader<T> { 
     InbMessage readInbMessage(int firstByte,IZmmudQueue<T> iacByteQueue);
     
}

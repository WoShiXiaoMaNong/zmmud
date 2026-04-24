package zm.mud.inbound.processor;

import zm.mud.inbound.message.InbMessage;

public interface InbMsgProcessor {

    boolean processMessage(InbMessage msg);

}

package zm.mud.network.inbound.processor;

import zm.mud.network.inbound.message.InbMessage;

public interface InbMsgProcessor {

    boolean processMessage(InbMessage msg);

}

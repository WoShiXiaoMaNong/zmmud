package zm.mud.network.inbound.processor;

import zm.mud.network.inbound.message.InbMsg;

public interface IInbMsgProcessor {

    boolean processMessage(InbMsg msg);

}

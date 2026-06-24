package zm.mud.core.network.inbound.processor;

import zm.mud.core.network.inbound.message.InbMsg;

public interface IInbMsgProcessor {

    boolean processMessage(InbMsg msg);

}

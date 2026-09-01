package zm.mud.core.network.inbound.processor;

import org.springframework.core.Ordered;

import zm.mud.core.network.inbound.message.InbMsg;

public interface IInbMsgProcessor extends Ordered {

    boolean processMessage(InbMsg msg);

}

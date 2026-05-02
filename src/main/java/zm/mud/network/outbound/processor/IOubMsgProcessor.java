package zm.mud.network.outbound.processor;

import zm.mud.network.outbound.message.OubMessage;

public interface OubMsgProcessor {

    boolean processMessage(OubMessage msg);

}

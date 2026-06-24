package zm.mud.core.network.outbound.processor;

import zm.mud.core.network.outbound.message.OubMsg;

public interface IOubMsgProcessor {

    boolean processMessage(OubMsg msg);

}

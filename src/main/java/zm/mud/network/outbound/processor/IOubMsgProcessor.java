package zm.mud.network.outbound.processor;

import zm.mud.network.outbound.message.OubMsg;

public interface IOubMsgProcessor {

    boolean processMessage(OubMsg msg);

}

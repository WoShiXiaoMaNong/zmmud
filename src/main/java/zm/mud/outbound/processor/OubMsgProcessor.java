package zm.mud.outbound.processor;

import zm.mud.outbound.message.OubMessage;

public interface OubMsgProcessor {

    boolean processMessage(OubMessage msg);

}

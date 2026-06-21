package zm.mud.core.trigger.matcher;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.trigger.cfg.MatchResult;

public interface IMatcher {
    MatchResult match(InbMsg msg);
    MatchResult match(OubMsg msg);
}

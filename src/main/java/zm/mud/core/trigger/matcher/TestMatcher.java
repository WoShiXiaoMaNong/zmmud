package zm.mud.core.trigger.matcher;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.outbound.message.NrmOubMsg;
import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.trigger.cfg.MatchResult;

public class TestMatcher implements IMatcher{

    @Override
    public MatchResult match(InbMsg msg) {
        return MatchResult.NOT_MATCHED(msg);
        
     
    }

    @Override
    public MatchResult match(OubMsg msg) {
       if( ! (msg instanceof NrmOubMsg)){
            return MatchResult.NOT_MATCHED(msg);
        }
        NrmOubMsg normalOubMsg = (NrmOubMsg) msg;
        String msgStr = normalOubMsg.getContent();
        if("fullme".equals(msgStr)){
            return MatchResult.MATCHED(normalOubMsg, null);
        }else{
            return MatchResult.NOT_MATCHED(msg);
        }

    }
    
}

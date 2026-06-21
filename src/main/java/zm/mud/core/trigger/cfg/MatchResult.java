package zm.mud.core.trigger.cfg;

import java.util.List;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.outbound.message.OubMsg;

public class MatchResult {
    
    private boolean matched;
    private InbMsg originInbMsg;
    private OubMsg originOubMsg;
    private List<String> matchedRet;

    public static MatchResult NOT_MATCHED(InbMsg msg){
        MatchResult ret = new MatchResult(false,msg,null,null);
        return ret;
    }

    public static MatchResult MATCHED(InbMsg msg,List<String> matchedRet){
        MatchResult ret = new MatchResult(true,msg,null,matchedRet);
        return ret;
    }

      public static MatchResult NOT_MATCHED(OubMsg msg){
        MatchResult ret = new MatchResult(false,null,msg,null);
        return ret;
    }

    public static MatchResult MATCHED(OubMsg msg,List<String> matchedRet){
        MatchResult ret = new MatchResult(true,null,msg,matchedRet);
        return ret;
    }

    

    public InbMsg getOriginInbMsg() {
        return originInbMsg;
    }

    public OubMsg getOriginOubMsg() {
        return originOubMsg;
    }

    public List<String> getMatchedRet() {
        return matchedRet;
    }




    public MatchResult(boolean matched, InbMsg originInbMsg, OubMsg originOubMsg, List<String> matchedRet) {
        this.matched = matched;
        this.originInbMsg = originInbMsg;
        this.originOubMsg = originOubMsg;
        this.matchedRet = matchedRet;
    }

    public boolean isMatched(){
        return this.matched;
    }
}

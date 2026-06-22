package zm.mud.core.trigger.cfg;

import java.util.List;

public class MatchResult {
    
    private boolean matched;
    private String originMsg;
    private List<String> matchedRet;

    public static MatchResult UNMATCHED(String msg){
        MatchResult ret = new MatchResult(false,msg,null);
        return ret;
    }

    public static MatchResult MATCHED(String msg,List<String> matchedRet){
        MatchResult ret = new MatchResult(true,msg,matchedRet);
        return ret;
    }

    public List<String> getMatchedRet() {
        return matchedRet;
    }


    public MatchResult(boolean matched, String originMsg, List<String> matchedRet) {
        this.matched = matched;
        this.originMsg = originMsg;
        this.matchedRet = matchedRet;
    }

    public boolean isMatched(){
        return this.matched;
    }

    public String getOriginMsg() {
        return originMsg;
    }
}

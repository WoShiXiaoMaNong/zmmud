package zm.mud.core.automation.trigger.matcher;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.automation.trigger.cfg.MatchResult;

@Component("MATCHER_Include")
@Scope("prototype")
public class Include extends AbsMatcher{

    @Override
    public MatchResult doMatch(String msg) {
   
        boolean isMatched = msg != null && msg.contains(this.getExpression());

        if(isMatched){
            return MatchResult.MATCHED(msg, null);
        }else{
            return MatchResult.UNMATCHED(msg);
        }

    }

    
    
}

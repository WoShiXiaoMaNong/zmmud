package zm.mud.core.automation.trigger.matcher;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.automation.trigger.cfg.MatchResult;

@Component("MATCHER_Equals")
@Scope("prototype")
public class Equals implements IMatcher{

    private String expression;


    @Override
    public MatchResult match(String msg) {
   
        boolean isMatched = msg != null && msg.equals(this.getExpression());

        if(isMatched){
            return MatchResult.MATCHED(msg, null);
        }else{
            return MatchResult.UNMATCHED(msg);
        }

    }


    @Override
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String getExpression() {
       return this.expression;
    }
    
    
}

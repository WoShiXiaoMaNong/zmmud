package zm.mud.core.trigger.matcher;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.trigger.cfg.MatchResult;

@Component("MATCHER_Include")
@Scope("prototype")
public class Include implements IMatcher{

    private String expression;


    @Override
    public MatchResult match(String msg) {
   
        boolean isMatched = msg != null && msg.contains(this.getExpression());

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

package zm.mud.core.trigger.action;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;

public interface IAction {
    
    void setExpression(String expression);
    String getExpression();

    void execute(Trigger trigger, MatchResult ret);
}

package zm.mud.core.trigger.action;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;

public interface IAction {
    
    void execute(Trigger tirgger, MatchResult ret);
}

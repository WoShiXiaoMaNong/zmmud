package zm.mud.core.trigger.action;

import java.util.Map;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;

public interface IAction {
    
    void setExpression(String expression);
    String getExpression();

    default void setParams(Map<String,Object> params){
        //do nothing;
    }
    default Object getParam(String paramKey){
        return null;
    }

    void execute(Trigger trigger, MatchResult ret);
}

package zm.mud.core.automation.action;

import java.util.Map;

public interface IAction {
    
    void setExpression(String expression);
    String getExpression();

    default void setParams(Map<String,Object> params){
        //do nothing;
    }
    default Object getParam(String paramKey){
        return null;
    }

    void execute(ActionContext context);
}

package zm.mud.core.trigger;




import zm.mud.core.trigger.action.IAction;
import zm.mud.core.trigger.cfg.TiggerType;
import zm.mud.core.trigger.matcher.IMatcher;

public class Trigger {
    
    private TiggerType triggerType;
    private String triggerName;
    private IMatcher matcher;
    private IAction action;

    

    public Trigger(TiggerType triggerType, String triggerName, IMatcher matcher, IAction action) {
        this.triggerType = triggerType;
        this.triggerName = triggerName;
        this.matcher = matcher;
        this.action = action;
    }
    public TiggerType getTriggerType() {
        return triggerType;
    }
    public void setTriggerType(TiggerType triggerType) {
        this.triggerType = triggerType;
    }
    public String getTriggerName() {
        return triggerName;
    }
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
    public IMatcher getMatcher() {
        return matcher;
    }
    public void setMatcher(IMatcher matcher) {
        this.matcher = matcher;
    }
    public IAction getAction() {
        return action;
    }
    public void setAction(IAction action) {
        this.action = action;
    }
    


    
    
}   

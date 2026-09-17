package zm.mud.core.automation.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("ACTION_SendCommand")
@Scope("prototype")
public class SendCommand implements IAction{
    private static final Logger log = LogManager.getLogger(SendCommand.class);

    private String expression;

    @Override
    public void execute(ActionContext context) {
        context.getSession().send(this.getExpression());
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

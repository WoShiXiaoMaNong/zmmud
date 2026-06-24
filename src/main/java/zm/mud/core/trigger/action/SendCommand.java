package zm.mud.core.trigger.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.api.OubMsgService;
import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.utils.SpringBeanUtil;

@Component("ACTION_SendCommand")
@Scope("prototype")
public class SendCommand implements IAction{
    private static final Logger log = LogManager.getLogger(SendCommand.class);

    private String expression;

    @Override
    public void execute(Trigger tirgger, MatchResult ret) {
        OubMsgService oubMsgService = SpringBeanUtil.getBean(OubMsgService.class);
        oubMsgService.send(this.getExpression());
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

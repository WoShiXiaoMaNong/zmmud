package zm.mud.core.automation.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.api.InbMsgService;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.MatchResult;
import zm.mud.core.automation.trigger.cfg.TriggerType;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;

@Component("ACTION_RegisterAction")
@Scope("prototype")
public class RegisterAction implements IAction{
    private static final Logger logger = LogManager.getLogger(RegisterAction.class);
    private String actionCfgJsonStr;


    @Override
    public void setExpression(String expression) {
        actionCfgJsonStr = expression;
    }

    @Override
    public String getExpression() {
        return this.actionCfgJsonStr;
    }

    @Override
    public void execute(MudSession session,Trigger trigger, MatchResult ret) {
        TriggerFactory tf = SpringBeanUtil.getBean(TriggerFactory.class);
        Trigger newTrigger = tf.buildByeName(session,this.getExpression());
        if( newTrigger == null){
            logger.debug("new tirgger is null : " + trigger.getTriggerName());
            return;
        }
        if(TriggerType.INBOUNG_TRIGGER.equals(newTrigger.getTriggerType())){
            InbMsgService is = SpringBeanUtil.getBean(InbMsgService.class);
            is.registerTrigger(session,newTrigger);
        }

        if(TriggerType.OUTBOUNG_TRIGGER.equals(newTrigger.getTriggerType())){
            OubMsgService os = SpringBeanUtil.getBean(OubMsgService.class);
            os.registerTrigger(session,newTrigger);
        }

    }
    
}

package zm.mud.pkuxkx.trigger.action;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.action.IAction;
import zm.mud.core.trigger.cfg.MatchResult;


/**
 * <pre>
 * 格式：
    #经验,潜能,最大内力,内力,最大精力,精力
    #气血上限,最大气血,气血,精神上限,最大精神,精神
    #真气,战意,食物,饮水,非战斗/战斗中,不忙/忙
    </pre>
 * HpbriefAction
 */
@Component("ACTION_HpbriefAction")
@Scope("prototype")
public class HpbriefAction implements IAction {
    private static final Logger logger = LogManager.getLogger(HpbriefAction.class);
    private String expression;

    @Override
    public void setExpression(String expression) {
        this.expression  = expression;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    public void execute(Trigger trigger, MatchResult ret) {
        List<String> rr = ret.getMatchedRet();
        logger.info(rr);
    }
    
}

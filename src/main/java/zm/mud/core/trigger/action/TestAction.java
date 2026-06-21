package zm.mud.core.trigger.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.cfg.MatchResult;

public class TestAction implements IAction{
    private static final Logger log = LogManager.getLogger(TestAction.class);
    @Override
    public void execute(Trigger tirgger, MatchResult ret) {
        log.info(ret.getOriginOubMsg().getContent());
    }
    
}

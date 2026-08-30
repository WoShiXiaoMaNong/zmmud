package zm.mud.core.automation.trigger.matcher;

import zm.mud.core.automation.trigger.cfg.MatchResult;

public interface IMatcher {
    MatchResult match(String msg);

    void setExpression(String expression);
    String getExpression();
}

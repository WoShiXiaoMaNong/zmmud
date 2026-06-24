package zm.mud.core.trigger.matcher;

import zm.mud.core.trigger.cfg.MatchResult;

public interface IMatcher {
    MatchResult match(String msg);

    void setExpression(String expression);
    String getExpression();
}

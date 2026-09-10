package zm.mud.core.automation.trigger.cfg;

import java.util.Map;

public class MatcherAndActionConfigEntry {
    private String type;
    private Boolean matchRawMsg;
    private String expression;
    private Map<String,Object> params;

  
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Boolean getMatchRawMsg() {
        return matchRawMsg;
    }
    public void setMatchRawMsg(Boolean matchRawMsg) {
        this.matchRawMsg = matchRawMsg;
    }
    @Override
    public String toString() {
        return "MatcherAndActionConfigEntry [type=" + type + ", matchRawMsg=" + matchRawMsg + ", expression="
                + expression + ", params=" + params + "]";
    }

    

}

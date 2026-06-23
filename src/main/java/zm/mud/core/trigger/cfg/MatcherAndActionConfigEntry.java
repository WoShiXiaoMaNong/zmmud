package zm.mud.core.trigger.cfg;

import java.util.Map;

public class MatcherAndActionConfigEntry {
     private String type;
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

    

}

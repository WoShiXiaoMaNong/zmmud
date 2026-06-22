package zm.mud.core.trigger.cfg;

public class TriggerConfigEntry {
    private String name;
    private String type;
    private Integer remainningCount;
    private MatcherAndActionConfigEntry matcher;
    private MatcherAndActionConfigEntry action;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Integer getRemainningCount() {
        return remainningCount;
    }
    public void setRemainningCount(Integer remainningCount) {
        this.remainningCount = remainningCount;
    }
    public MatcherAndActionConfigEntry getMatcher() {
        return matcher;
    }
    public void setMatcher(MatcherAndActionConfigEntry matcher) {
        this.matcher = matcher;
    }
    public MatcherAndActionConfigEntry getAction() {
        return action;
    }
    public void setAction(MatcherAndActionConfigEntry action) {
        this.action = action;
    }

    
}

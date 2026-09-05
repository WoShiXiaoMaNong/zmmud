package zm.mud.core.automation.trigger.cfg;

public class TriggerConfigEntry {
    private String name;
    private String type;
    private Integer remainingCount;
    private MatcherAndActionConfigEntry matcher;
    private MatcherAndActionConfigEntry action;

    /**
     * 不配置时，为false。
     * true: trigger 会在独立的线程中执行
     * false: trigger 会在主线程中执行（对于outbound trigger，会在执行完整个trigger后，消息才会被发出去）
     */
    private Boolean sync;

    /**
     * 不配置时，为false
     * true: 不可以同时注册多个相同的trigger
     * false: 可以同时注册多个相同的trigger
     */
    private Boolean unique;

    /**
     * 不配置时，为false
     * true: 启动时，自动注册
     * false: 启动时，不自动注册
     */
    private Boolean autoRegister;

    public void setAutoRegister(Boolean autoRegister){
        this.autoRegister = autoRegister;
    }

    public boolean isAutoRegister(){
        return Boolean.TRUE.equals(this.autoRegister);
    }

    public void setUnique(Boolean unique){
        this.unique = unique;
    }

    public Boolean isUnique(){
        return Boolean.TRUE.equals(this.unique);
    }

    /**
     * 当sync是空时，返回false，即：配置表中不配置的时候，默认为false
     * @return
     */
    public Boolean isSync() {
        return Boolean.TRUE.equals(sync);
    }

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
    public Integer getRemainingCount() {
        return remainingCount;
    }
    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
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
    
    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    
}

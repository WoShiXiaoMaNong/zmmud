package zm.mud.core.automation.timer.cfg;

import java.util.HashMap;
import java.util.Map;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;

/**
 * 定时器配置实体类（严格匹配 JSON 结构）
 */
public class TimerConfigEntry {
    private String id;
    private String name;
    private boolean enable;
    private String type;          // "delay" 或 "cron"
    private String triggerValue;  // 延迟的毫秒数（如 "3500"）或 Cron 表达式（如 "0 0 0 * * ?"）
    private MatcherAndActionConfigEntry action; // 复用 Trigger 的动作配置结构
    private Integer remainingCount;

    // 运行时辅助字段（不一定序列化到 JSON，但用于 Monitor 监控表格的实时展示）
    private transient Map<String, Object> runtimeParams = new HashMap<>();

    public TimerConfigEntry() {
        this.action = new MatcherAndActionConfigEntry();
    }

    // ─── Getters 和 Setters ───
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }


    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(String triggerValue) {
        this.triggerValue = triggerValue;
    }

    public MatcherAndActionConfigEntry getAction() { return action; }
    public void setAction(MatcherAndActionConfigEntry action) { this.action = action; }

    public Map<String, Object> getRuntimeParams() { return runtimeParams; }

    public Integer getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
    }
    
}

package zm.mud.core.automation.timer;

import java.util.function.Consumer;

public class MudTimer implements Comparable<MudTimer> {
    private final long id;
    private long nextFireTime; 
    private final long period;  
    private final boolean fixedRate;
    private final Consumer<MudTimer> action; 
    private boolean cancelled = false;

    /**
     * <pre>
     * fixedRate:
     * 1. false : Fixed-Rate -> 基于理论上的上一次触发时间累加（不受卡顿和执行耗时影响）
     * 2. true  : Fixed-Delay: 基于【本次实际执行结束的时间】累加
     * </pre>
     * @param id
     * @param delay
     * @param period
     * @param fixedRate
     * @param action
     */
    public MudTimer(long id, long delay, long period, boolean fixedRate, Consumer<MudTimer> action) {
        this.id = id;
        this.nextFireTime = System.currentTimeMillis() + delay;
        this.period = period;
        this.fixedRate = fixedRate;
        this.action = action;
    }

    public long getId() { return id; }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }
    public boolean isPeriodic() { return period > 0; }
    public long getNextFireTime() { return nextFireTime; }

    // 区分 Fixed-Rate 和 Fixed-Delay
    public void updateNextFireTime(long executionEndTime) {
        if (fixedRate) {
            // Fixed-Rate: 基于理论上的上一次触发时间累加（不受卡顿和执行耗时影响）
            this.nextFireTime = this.nextFireTime + period;
        } else {
            // Fixed-Delay: 基于【本次实际执行结束的时间】累加
            // 确保“上一次结束”到“下一次开始”之间，永远有固定的 period 毫秒空闲 [1]
            this.nextFireTime = executionEndTime + period;
        }
    }

    public void execute() {
        if (!cancelled && action != null) {
            action.accept(this);
        }
    }

    @Override
    public int compareTo(MudTimer o) {
        return Long.compare(this.nextFireTime, o.nextFireTime);
    }
}

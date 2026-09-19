package zm.mud.core.automation.timer;


import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

@Service
public class TimerManager {
    // 最小堆：自动将最近要触发的 Timer 排在最前面
    private final PriorityQueue<MudTimer> timerQueue = new PriorityQueue<>();
    // 方便通过 ID 快速查找和取消 Timer（比如玩家主动停止某个长脚本）
    private final Map<Long, MudTimer> timerMap = new HashMap<>();
    private long idSequence = 0;

    /**
     * <pre>
     * 添加定时器（支持单次和循环)
     * Delay 和 period都是毫秒
     * 注：这是Fixed-Delay -> 基于【本次实际执行结束的时间】累加
     * 首选 Fixed-Delay模式，比较安全，不会引起Timer堆积
     * </pre>
     * @param delay  首次触发延迟
     * @param period 第一次触发完成之后，后续每隔多少毫秒再次触发一次。（注：0 表示单次执行）
     * @param action 触发时的回调函数
     * @return
     */
    public long schedule(long delay, long period, Consumer<MudTimer> action) {
        return schedule(delay, period, false, action);
    }

    /**
     * <pre>
     * 添加定时器（支持单次和循环)
     * Delay 和 period都是毫秒
     * fixedRate:
     * 1. false : Fixed-Rate -> 基于理论上的上一次触发时间累加（不受卡顿和执行耗时影响）
     * 2. true  : Fixed-Delay: 基于【本次实际执行结束的时间】累加
     * </pre>
     * @param delay  首次触发延迟
     * @param period 第一次触发完成之后，后续每隔多少毫秒再次触发一次。（注：0 表示单次执行）
     * @param action 触发时的回调函数
     * @return
     */
    public synchronized long schedule(long delay, long period, boolean fixedRate, Consumer<MudTimer> action) {
        long id = ++idSequence;
        MudTimer timer = new MudTimer(id, delay, period, fixedRate, action);
        timerQueue.add(timer);
        timerMap.put(id, timer);
        return id;
    }

    // 取消定时器
    public synchronized void cancel(long id) {
        MudTimer timer = timerMap.remove(id);
        if (timer != null) {
            timer.cancel(); // 标记删除，等到 tick 到它时自动丢弃
        }
    }

    /**
     * <pre>
     *  1. 获取距离下一个 Timer 触发还有多少毫秒（用于让主线程精准 Sleep）
     *  2. 当队列为空时： 返回 -1
     * </pre>
     * @return
     */
    public synchronized long getNextDelay() {
        // 清理掉堆顶已经被取消的垃圾 Timer
        while (!timerQueue.isEmpty() && timerQueue.peek().isCancelled()) {
            timerQueue.poll();
        }
        if (timerQueue.isEmpty()) {
            return -1; // 没有定时器了
        }
        long delay = timerQueue.peek().getNextFireTime() - System.currentTimeMillis();
        return Math.max(0, delay); // 不能返回负数
    }

    // 核心轮询驱动方法：由主循环不断调用
    public synchronized void tick() {
        long now = System.currentTimeMillis();

        while (!timerQueue.isEmpty()) {
            MudTimer top = timerQueue.peek();

            if (top.getNextFireTime() > now) {
                break;
            }

            timerQueue.poll();

            if (!top.isCancelled()) {
                // 1. 执行任务
                top.execute();

                // 2. 任务执行完后，如果是周期性任务，计算下一次时间
                if (top.isPeriodic() && !top.isCancelled()) {
                    // 核心修正：在任务【执行完后】获取当前时间，作为 Fixed-Delay 的基准 [1]
                    long executionEndTime = System.currentTimeMillis();
                    top.updateNextFireTime(executionEndTime);

                    // 3. 重新放回堆中
                    timerQueue.add(top);
                } else {
                    timerMap.remove(top.getId());
                }
            } else {
                timerMap.remove(top.getId());
            }
        }
    }


    public synchronized void reloadTimer(){
        this.timerMap.clear();
        this.timerQueue.clear();

        
    }
}

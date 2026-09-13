package zm.mud.core.session;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class CommandQue <T> {
    // 玩家特有的出站命令队列
    private final Deque<T> oubCommandQueue = new ArrayDeque<>();
    // 标记当前是否有命令正在执行/等待中，防止并发冲突
    private volatile boolean isExecuting = false;
    private Lock commandLock = new ReentrantLock();


    public void addCommands(List<T> cmds) {
        try{
            commandLock.lock();
            this.oubCommandQueue.addAll(cmds);
        }finally{
            commandLock.unlock();
        }
        
    }

    /**
     * <pre>
     * 当队列为空时：
     * 1. 返回null
     * 2. 更新isExecuting为false
     * </pre>
     */
    public T pollCommand() {
         try{
            commandLock.lock();
            T cmd =  this.oubCommandQueue.poll();
            if(cmd == null){
                this.isExecuting = false;
            }
            return cmd;
        }finally{
            commandLock.unlock();
        }
    }

    public void pushCommand(List<T> cmds) {
        if(cmds == null || cmds.isEmpty()){
            return;
        }
        try{
            commandLock.lock();
            for(int i = cmds.size() - 1; i >=0 ; i--){
                this.oubCommandQueue.push(cmds.get(i));
            }
        }finally{
            commandLock.unlock();
        }
       
    }

    /**
     * 检查当前是否有命令在执行，如果没有，则直接强占（设为 true）并返回 true；
     * 如果已经被抢占，则返回 false。
     * 外部调用时只需：if (commandQue.tryToExecute()) { ... 放心处理指令 ... }
     */
    public boolean tryToExecute() {
        try {
            commandLock.lock();
            if (!isExecuting) {
                isExecuting = true;
                return true; // 成功抢到执行权
            }
            return false; // 已经在执行中，抢占失败
        } finally {
            commandLock.unlock();
        }
    }

    /**
     * 当某条命令（或连招）完全执行/结算完毕后，由执行线程调用该方法释放执行权，
     * 从而允许后续的下一条命令开始被调度执行。
     */
    public void finishExecuting() {
        try {
            commandLock.lock();
            this.isExecuting = false;
        } finally {
            commandLock.unlock();
        }
    }


    public int size() {
        try {
            commandLock.lock();
            return this.oubCommandQueue.size();
        } finally {
            commandLock.unlock();
        }
    }
    
    public void clearCommands() {
        try{
            commandLock.lock();
            this.oubCommandQueue.clear();
            this.isExecuting = false;
        }finally{
            commandLock.unlock();
        }
    }

}

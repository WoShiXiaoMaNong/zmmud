package zm.mud.core.trigger;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.trigger.action.IAction;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.core.trigger.cfg.TriggerType;
import zm.mud.core.trigger.matcher.IMatcher;

public class Trigger {
    private static final Logger logger = LogManager.getLogger(Trigger.class);
    private TriggerType triggerType;
    private String triggerName;
    private IMatcher matcher;
    private IAction action;
    

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

    
    /*
       1. 用于记录触发器还可以被触发几次。
       2. 当归零时，该触发器将会被销毁
       3. 设置一个小于零的数值，来表示该触发器永久有效
     */
    private volatile int remainingTriggerCount; 

 
    /**
     * 
     * @param triggerType
     * @param triggerName
     * @param matcher
     * @param action
     * @param maxTriggerCount 
     */
    public Trigger(TriggerType triggerType, String triggerName, IMatcher matcher, IAction action,int maxTriggerCount) {
        this.triggerType = triggerType;
        this.triggerName = triggerName;
        this.matcher = matcher;
        this.action = action;
        this.remainingTriggerCount = maxTriggerCount;
    }


    public MatchResult match(String msg){
        return this.matcher.match(msg);
    }

    public synchronized void countDownRemaining(){
        if( this.died() ){
            return;
        }
        this.remainingTriggerCount --;
    }

    public void fire(MatchResult ret){
        if(this.died()){
            return;
        }
        
        this.countDownRemaining();

        if(this.action == null){
            return;
        }
        try{
            this.action.execute(this, ret);
        }catch(Exception e){
            logger.error(this.getTriggerName() + " error!",e);
        }
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    public String getTriggerName() {
        return triggerName;
    }
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public void setMatcher(IMatcher matcher) {
        this.matcher = matcher;
    }

    public void setAction(IAction action) {
        this.action = action;
    }

       public boolean died(){
        return this.remainingTriggerCount == 0;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getUniqueKey(){
        return this.triggerName;
    }

    public void setAutoRegister(boolean autoRegister){
        this.autoRegister = autoRegister;
    }

    public boolean isAutoRegister(){
        return this.autoRegister;
    }

    public void setUnique(boolean unique){
        this.unique = unique;
    }

    public boolean isUnique(){
        return this.unique;
    }

    
}   

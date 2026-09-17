package zm.mud.core.automation.timer;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.automation.action.ActionContext;
import zm.mud.core.automation.action.IAction;
import zm.mud.core.automation.action.SendCommandAction ;
import zm.mud.core.automation.timer.timer.MudTimer;
import zm.mud.core.automation.timer.timer.TimerManager;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;

@Service 
public class TimerService {
    

    @Autowired 
    private TimerManager timerManager;

    public void commandTicker(MudSession session, String cmd,long delay){
        IAction action = SpringBeanUtil.getBean(SendCommandAction.class);  
        action.setExpression(cmd);
        ActionContext context = new ActionContext(session);

        timerManager.schedule(delay, delay, new Consumer<MudTimer>() {

            @Override
            public void accept(MudTimer t) {
                action.execute(context);
            }
            
        });
    }
}

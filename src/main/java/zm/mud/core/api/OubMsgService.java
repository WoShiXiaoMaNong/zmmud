package zm.mud.core.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zm.mud.core.api.oub.IOubCommand;
import zm.mud.core.api.oub.OubCommandParser;
import zm.mud.core.api.oub.cmd.NormalOubCommand;
import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.network.outbound.message.NrmOubMsg;
import zm.mud.core.network.outbound.processor.OubTriggerProcessor;
import zm.mud.core.network.queue.OubMsgQueue;
import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPool;

@Service
public class OubMsgService {
    private static final Logger logger = LogManager.getLogger(OubMsgService.class);

    @Autowired
    private OubMsgQueue oubMsgQueue;

    @Autowired
    private OubTriggerProcessor triggerProcessor;

    @Autowired 
    private OubCommandParser oubCommandParser;
    

    public void sendCommand(MudSession session, String commandMsg) {
        List<IOubCommand> commands = oubCommandParser.parse(session, commandMsg);
       

        ZmmudThreadPool.execute(() -> {

            // 关于这部分的锁，后续需要优化，目前的锁颗粒度很大
            synchronized(session){
                session.addCommands(commands);
                // 如果当前已经在执行或处于等待延时状态，退出，让原有的逻辑继续走
                if (session.isCommandExecuting()) {
                    return;
                }
                session.setCommandExecuting(true);

            
                IOubCommand cmd = session.pollCommand();
                while(cmd != null){
                    if( cmd instanceof NormalOubCommand){
                        this.senddirectly(session,cmd.getCommandStr());
                    }else{
                        cmd.exec();
                    }
                    cmd = session.pollCommand();
                }
                session.setCommandExecuting(false);
            
            }
        });

    }
    
    public void senddirectly(MudSession session,String msStr){
        session.echoCommandToUI( msStr);
         this.oubMsgQueue.put(session,new NrmOubMsg(session,msStr));
    }

    public void registerTrigger(MudSession session,Trigger trigger){
        if( trigger == null ){
            logger.warn("Trigger is null. Skip!");
            return;
        }
        triggerProcessor.register(session,trigger);
    }

    public void cleanTrigger(MudSession session) {
        triggerProcessor.cleanTrigger(session);
    }

}

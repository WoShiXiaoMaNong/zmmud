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
            // 1. 先把新命令安全地放入队列
            session.addCommands(commands);

            // 2. 核心状态循环：确保新放入的命令一定会被执行
            while (true) {
                // 尝试抢占执行权
                if (!session.tryToExecute()) {
                    // 如果抢占失败，说明已经有另一个线程在消费队列了。
                    // 刚才我们通过 addCommands 放入的命令，会被那个正在执行的线程在 while 循环里顺便消费掉，
                    // 所以当前线程可以安全地退出。
                    return;
                }

                try {
                    // 抢占成功，开始消费队列中的所有命令
                    IOubCommand cmd = session.pollCommand();
                    while (cmd != null) {
                        if (cmd instanceof NormalOubCommand) {
                            this.senddirectly(session, cmd.getCommandStr());
                        } else {
                            cmd.exec();
                        }
                        cmd = session.pollCommand();
                    }
                } catch(Exception e){
                    logger.error("Excute Cmd error!",e);
                    // 正常情况，会在 session.pollCommand();方法送，发现queue为空的时候，自动将executing设置为false
                    // 只有异常情况，才需要手动设置，允许别的进程尝试来获取。
                    session.finishExecuting();
                }

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

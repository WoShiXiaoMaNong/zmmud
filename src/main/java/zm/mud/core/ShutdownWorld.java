package zm.mud.core;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import zm.mud.core.network.inbound.message.NormalInbMsg;
import zm.mud.core.network.queue.InbMsgQueue;
import zm.mud.core.network.threads.ThreadPoolService;
import zm.mud.core.session.MudSession;

@Service
public class ShutdownWorld extends Thread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ShutdownWorld.class);

    @Autowired
    private ThreadPoolService threadPoolService;

    @Autowired
    private InbMsgQueue inbMsgQueue;

    @Autowired
    private ApplicationContext context;
    
    @PostConstruct
    public void init() {
        this.setName("ShutdownWorld");
        Runtime.getRuntime().addShutdownHook(this);
        logger.info("ShutdownWorld initialized and ready to handle shutdown tasks.");
    }

    @Override
    public void run() {
        logger.info("ShutdownWorld is running. Performing cleanup tasks...");
        for(Entry<String,MudSession> entry : MudSession.allSession().entrySet()){
            MudSession session = entry.getValue();
            this.inbMsgQueue.put(session,new NormalInbMsg(session,"按任意键后退出。。。"));
        }
        
            if (context instanceof AbstractApplicationContext) {
                ((AbstractApplicationContext) context).close();
            }

      
            try {
                threadPoolService.shutdownAll();
            } catch (Exception e) {
                logger.error("Error occurred while executing shutdown", e);
            }
        
        logger.info("Cleanup tasks completed. ShutdownWorld is exiting.");
    }

}

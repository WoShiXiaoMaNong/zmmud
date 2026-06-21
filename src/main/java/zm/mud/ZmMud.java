package zm.mud;


import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import zm.mud.client.MudClient;
import zm.mud.network.threads.ThreadPoolService;
import zm.mud.ui.ZmMudUI;

/**
 * Hello world!
 */
public class ZmMud {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZmMud.class);

    private static final ApplicationContext  context = new AnnotationConfigApplicationContext("zm.mud");

    public static void main(String[] args) throws UnknownHostException, IOException {
        ZmMud app = new ZmMud();
        app.start();
    }


    public void start(){
        ZmMudUI zmMudUI = context.getBean(ZmMudUI.class);
        zmMudUI.start();
        MudClient client = context.getBean(MudClient.class);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error("Slowdown error",e);
        }
        boolean isConnected = client.connect();


        if(!isConnected) {
            logger.error("Failed to connect to server");
            return;
        }
        logger.info("Connected to server successfully");
      
        ThreadPoolService threadStarter = context.getBean(ThreadPoolService.class);
        threadStarter.startAllThreads();

       

    }

}

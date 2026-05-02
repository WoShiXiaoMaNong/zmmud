package zm.mud;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import zm.mud.client.MudClient;
import zm.mud.network.outbound.message.NormalOutboundMsg;
import zm.mud.network.outbound.message.OubMessage;
import zm.mud.network.queue.OubMsgQueue;
import zm.mud.network.utils.SubThreadUtil;

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
        
        MudClient client = context.getBean(MudClient.class);

        boolean isConnected = client.connect();

        if(!isConnected) {
            logger.error("Failed to connect to server");
            return;
        }
        logger.info("Connected to server successfully");

        SubThreadUtil threadStarter = context.getBean(SubThreadUtil.class);
        threadStarter.startAllThreads();

        OubMsgQueue oubMsgQueue = context.getBean(OubMsgQueue.class);
          new Thread(() -> {
            try {
                BufferedReader consoleReader = new BufferedReader(
                    new InputStreamReader(System.in, Charset.forName("GBK"))
                );
                String line = null;
                while ((line = consoleReader.readLine()) != null) {
                  OubMessage msg = new NormalOutboundMsg(line);
                  oubMsgQueue.put(msg);
                }
            } catch (IOException e) {
                logger.error("Failed to read from console", e);
            }
        }).start();
    }

    public static ApplicationContext getContext() {
        return context;
    }
}

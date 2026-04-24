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
import zm.mud.outbound.message.NormalOutboundMsg;
import zm.mud.outbound.message.OubMessage;
import zm.mud.queue.OubMsgQueue;
import zm.mud.threads.SubThreadUtil;

/**
 * Hello world!
 */
public class ZmMud {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZmMud.class);

    public static void main(String[] args) throws UnknownHostException, IOException {
        ApplicationContext  context = new AnnotationConfigApplicationContext("zm.mud");
        MudClient client = context.getBean(MudClient.class);

        client.connect("mud.pkuxkx.net", 8080, null);

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
}

package zm.mud.network.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.outbound.message.NrmOubMsg;
import zm.mud.network.outbound.message.OubMsg;
import zm.mud.network.queue.OubMsgQueue;

// @Service
public class ConsoleInputThread  extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ConsoleInputThread.class);

    @Autowired
    private OubMsgQueue oubMsgQueue;

    private  BufferedReader consoleReader;

    @Override
    protected void beforeLoop() {
      
        if (consoleReader != null) {
            return; 
        }
       
        if (System.console() == null) {
            logger.warn("No console available, ConsoleInputThread will not be started.");
            return;
        }
         consoleReader = new BufferedReader(
                    new InputStreamReader(System.in, Charset.forName("GBK"))
        );
    }

    @Override
    public boolean doRun() {
        if(this.consoleReader == null) {
            logger.error("Console reader is not initialized");
            return false;
        }
        try {
            String line = consoleReader.readLine();
            OubMsg msg = new NrmOubMsg(line);
            oubMsgQueue.put(msg);
        } catch (IOException e) {
            logger.error("Read message from console error!",e);
        }
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            if (consoleReader != null) {
                consoleReader.close(); 
            }
        } catch (Exception e) {
            logger.error("Failed to close console reader", e);
        }
    }

}

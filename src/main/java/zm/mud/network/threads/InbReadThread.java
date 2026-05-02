package zm.mud.network.threads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.inbound.reader.InbMessageReader;


@Service
public class InbReadThread implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbReadThread.class);
    
   
    @Autowired
    private MudClient client;

    @Autowired
    private InbMessageReader reader;

 
    private volatile boolean running = true;

    private Thread workerThread;

   
    @Override
    public void shutdown() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                reader.handleByte(client.read(),client.getCharset());
            } catch (Exception e) {
                logger.error("Failed to read from server", e);
            }
        }
    }

  

}

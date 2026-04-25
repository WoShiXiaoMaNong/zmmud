package zm.mud.network.threads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.reader.IacInbMsgReader;
import zm.mud.network.queue.InbByteIACQueue;
import zm.mud.network.queue.InbMsgQueue;
@Service
public class InboundIacMsgCollectThread implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InboundIacMsgCollectThread.class);
    @Autowired
    private InbMsgQueue msgProcessor;

    @Autowired
    private InbByteIACQueue iacByteQueue;
    
    @Autowired
    private IacInbMsgReader iacInbMsgReader;


    private volatile boolean running = true;
    private Thread workerThread;

    @Override
    public void shutdown() {
        running = false;
        if(workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                int firstByte = iacByteQueue.take();
                InbMessage inbMsg = null;
                if( firstByte == 255) {
                    inbMsg = iacInbMsgReader.readInbMessage(firstByte, iacByteQueue);
                }
                msgProcessor.put(inbMsg);
            } catch (Exception e) {
                logger.error("Failed to read from server", e);
            }
        }

    }

}

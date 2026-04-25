package zm.mud.network.threads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.reader.MudGameMsgReader;
import zm.mud.network.queue.InbMsgQueue;
import zm.mud.network.queue.raw.InbByteMudGameMsgQueue;

@Service
public class InboundMudMsgCollectThread implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InboundMudMsgCollectThread.class);
    @Autowired
    private InbMsgQueue inbMsgQueue;

    @Autowired
    private InbByteMudGameMsgQueue mudGameMsgByteQueue;

    @Autowired
    private MudGameMsgReader mudGameMsgReader;

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
                int firstByte = mudGameMsgByteQueue.take();
                InbMessage inbMsg = mudGameMsgReader.readInbMessage(firstByte, mudGameMsgByteQueue);
                inbMsgQueue.put(inbMsg);
            } catch (Exception e) {
                logger.error("Failed to read from server", e);
            }
        }

    }

}

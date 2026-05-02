package zm.mud.network.threads;

import org.springframework.beans.factory.annotation.Autowired;

import zm.mud.client.MudClient;
import zm.mud.network.queue.InbMsgQueue;

public class InbReadThread implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbReadThread.class);
    
    enum InbReaderState {
        NOT_STARTED,
        NORMAL_READING,
        NORMAL_END,
        IAC_START,
        IAC_COMMAND,
        IAC_SUBNEGOTIATION,
        IAC_SUBNEGOTIATION_END
    }
    @Autowired
    private MudClient client;

    @Autowired
    private InbMsgQueue inbMsgQueue;


    private InbReaderState state = InbReaderState.NOT_STARTED;

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
                handleByte(client.read());
            } catch (Exception e) {
                logger.error("Failed to read from server", e);
            }
        }
    }


    private void handleByte(int currentByte) {
        switch (state) {
            case NOT_STARTED:
            case NORMAL_READING:
                if (currentByte == 255) { // IAC
                    state = InbReaderState.IAC_START;
                } else {
                    state = InbReaderState.NORMAL_READING;
                    inbMsgQueue.put(client.getGameMsgReader().readInbMessage(currentByte, client.getIacByteQueue()));
                }
                break;
            case IAC_START:
                client.getIacByteQueue().put(currentByte);
                if (currentByte == 255) { // IAC
                    state = InbReaderState.IAC_START; // stay in IAC_START to handle consecutive IAC bytes
                } else if (currentByte == 250) { // SB
                    state = InbReaderState.IAC_SUBNEGOTIATION;
                } else if (currentByte == 240) { // SE
                    state = InbReaderState.NORMAL_READING;
                } else if (currentByte >= 241 && currentByte <= 249) { // other non-option commands
                    state = InbReaderState.NORMAL_READING;
                } else if (currentByte >= 251 && currentByte <= 254) { // option negotiation commands
                    state = InbReaderState.IAC_COMMAND;
                } else {
                    logger.warn("Received unknown IAC command: " + currentByte);
                    state = InbReaderState.NORMAL_READING; // fallback to normal reading
                }
                break;
            case IAC_COMMAND:
                client.getIacByteQueue().put(currentByte);
                state = InbReaderState.NORMAL_READING; // after command byte, go back to normal reading
                break;
            case IAC_SUBNEGOTIATION:
                client.getIacByteQueue().put(currentByte);
                if (currentByte == 240) { // SE
                    state = InbReaderState.NORMAL_READING;
                }
                break;
            default:
                logger.warn("Invalid reader state: " + state);
                state = InbReaderState.NORMAL_READING; // reset to normal reading on invalid state
        }
    }


  

}

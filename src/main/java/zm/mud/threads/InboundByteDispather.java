package zm.mud.threads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.inbound.processor.IACConfirmProcessor;
import zm.mud.queue.InbByteIACQueue;
import zm.mud.queue.InbByteMudGameMsgQueue;

@Service
public class InboundByteDispather implements ZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InboundByteDispather.class);

    @Autowired
    private MudClient client;

    @Autowired
    private InbByteIACQueue iacByteQueue;

    @Autowired
    private InbByteMudGameMsgQueue mudGameMsgByteQueue;

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
                int currentByte = client.read();
                if (currentByte == 255) {
                    handleIACByte(currentByte);
                } else {
                    handleMudGameByte(currentByte);
                }
            } catch (Exception e) {
                logger.error("Failed to read from server", e);
            }
        }

    }

    private void handleMudGameByte(int currentByte) {
        mudGameMsgByteQueue.put(currentByte);
    }

    private void handleIACByte(int firstByte) {
        // For now, we simply put the IAC byte into the IAC queue. Future we can add
        // more complex handling logic here if needed.
        iacByteQueue.put(firstByte);
        int commandByte = client.read();
        iacByteQueue.put(commandByte);

        if (IACConfirmProcessor.NON_OPTION_COMMANDS.contains(commandByte)) { // These commands do not have an option
                                                                             // byte, we can directly log them and
                                                                             // return.
            logger.debug("Received IAC command from server: "
                    + IACConfirmProcessor.CMD_NAME_MAP.getOrDefault(commandByte, "UNKNOWN"));
            return;
        } else if (commandByte == IACConfirmProcessor.CMD_SB) { // Subnegotiation command, we need to read until we
                                                                // encounter IAC SE
            // format 255 CMD_SB <opt> ...data... 255 CMD_SE
            logger.debug("Received IAC SB command, starting to read subnegotiation content...");

            while (true) {
                int b = client.read();
                if (b == 255) { // IAC
                    int next = client.read();
                    if (next == IACConfirmProcessor.CMD_SE) {
                        break; // end of subnegotiation
                    } else {
                        iacByteQueue.put(b);
                        iacByteQueue.put(next);
                    }
                } else {
                    iacByteQueue.put(b);
                }
            }
            logger.debug("Finished reading subnegotiation content.");

        } else { // For option negotiation commands, we need to read the option byte as well.
            int opt = client.read();
            iacByteQueue.put(opt);
        }

    }

}

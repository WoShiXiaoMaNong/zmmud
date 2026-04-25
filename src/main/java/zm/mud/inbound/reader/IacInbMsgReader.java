package zm.mud.inbound.reader;

import org.springframework.stereotype.Service;

import zm.mud.inbound.message.InbMessage;
import zm.mud.inbound.message.NormalInbMsg;
import zm.mud.inbound.processor.IACConfirmProcessor;
import zm.mud.queue.ZmmudQueue;

@Service
public class IacInbMsgReader implements InbMessageReader<Integer> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(IacInbMsgReader.class);

    @Override
    public InbMessage readInbMessage(int firstByte, ZmmudQueue<Integer> iacByteQueue) {
        int cmd = iacByteQueue.take();
        if (IACConfirmProcessor.NON_OPTION_COMMANDS.contains(cmd)) {
            logger.debug("Received IAC command from server: "
                    + IACConfirmProcessor.CMD_NAME_MAP.getOrDefault(cmd, "UNKNOWN"));
            return new NormalInbMsg(""); // or a specific IAC message object
        }
        if (cmd == IACConfirmProcessor.CMD_SB) {
            // Handle subnegotiation
            logger.debug("Received IAC SB command, starting to read subnegotiation content...");
            // For simplicity, we will just read until we encounter IAC SE. In a real
            // implementation, you would want to handle this more robustly.
            StringBuilder sbContent = new StringBuilder();
            while (true) {
                int b = iacByteQueue.take();
                if (b == 255) { // IAC
                    int next = iacByteQueue.take();
                    if (next == IACConfirmProcessor.CMD_SE) {
                        break; // end of subnegotiation
                    } else {
                        sbContent.append((char) b).append((char) next); // append the IAC and the next byte as content
                    }
                } else {
                    sbContent.append((char) b);
                }
            }
            logger.debug("Finished reading subnegotiation content: " + sbContent.toString());
            return new NormalInbMsg(sbContent.toString()); // or a specific SubnegotiationInbMsg object
        }
        // Currently we only handle option negotiation commands, other IAC commands are
        // treated as normal messages. Future we can add specific handling for other IAC
        // commands if needed.

        int opt = iacByteQueue.take();

        return InbMessage.buildIACConfirmMsg(new int[] { firstByte, cmd, opt });
    }

}

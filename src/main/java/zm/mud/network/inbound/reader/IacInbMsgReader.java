package zm.mud.network.inbound.reader;

import org.springframework.stereotype.Service;

import zm.mud.network.inbound.consts.IACConsts;
import zm.mud.network.inbound.message.InbMessage;
import zm.mud.network.inbound.message.NormalInbMsg;
import zm.mud.network.queue.IZmmudQueue;

@Service
public class IacInbMsgReader implements InbMessageReader<Integer> {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(IacInbMsgReader.class);

    @Override
    public InbMessage readInbMessage(int firstByte, IZmmudQueue<Integer> iacByteQueue) {
        int cmd = iacByteQueue.take();
        if (IACConsts.NON_OPTION_COMMANDS.contains(cmd)) {
            logger.debug("Received IAC command from server: "
                    + IACConsts.CMD_NAME_MAP.getOrDefault(cmd, "UNKNOWN"));
            return new NormalInbMsg(""); // or a specific IAC message object
        }
        if (cmd == IACConsts.CMD_SB) {
            // Handle subnegotiation
            logger.debug("Received IAC SB command, starting to read subnegotiation content...");
            // For simplicity, we will just read until we encounter IAC SE. In a real
            // implementation, you would want to handle this more robustly.
            StringBuilder sbContent = new StringBuilder();
            while (true) {
                int b = iacByteQueue.take();
                if (b == 255) { // IAC
                    int next = iacByteQueue.take();
                    if (next == IACConsts.CMD_SE) {
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

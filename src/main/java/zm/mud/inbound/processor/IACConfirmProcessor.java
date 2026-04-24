package zm.mud.inbound.processor;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.inbound.message.InbMessage;
import zm.mud.inbound.message.IACConfirmInbMsg;

@Service
public class IACConfirmProcessor implements InbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(IACConfirmProcessor.class);

    public static final int CMD_SE = 240; // subnegotiation end
    public static final int CMD_NOP = 241;
    public static final int CMD_DM = 242;
    public static final int CMD_BRK = 243;
    public static final int CMD_IP = 244;
    public static final int CMD_AO = 245;
    public static final int CMD_AYT = 246;
    public static final int CMD_EC = 247;
    public static final int CMD_EL = 248;
    public static final int CMD_GA = 249; // go ahead
    public static final int CMD_SB = 250; // subnegotiation begin

    public static final Set<Integer> NON_OPTION_COMMANDS = Set.of(
            CMD_SE, CMD_NOP, CMD_DM, CMD_BRK, CMD_IP, CMD_AO, CMD_AYT, CMD_EC, CMD_EL, CMD_GA);

    public static final int CMD_WILL = 251;
    public static final int CMD_WONT = 252;
    public static final int CMD_DO = 253;
    public static final int CMD_DONT = 254;

    public static final int OPTION_MXP0 = 90;
    public static final int OPTION_MXP1 = 91;

    public static final Map<Integer, String> CMD_NAME_MAP = Map.of(
            CMD_GA, "GA",
            CMD_DONT, "DONT",
            CMD_DO, "DO",
            CMD_WILL, "WILL",
            CMD_WONT, "WONT");

    public static final Map<Integer, Integer> ACCEPT_RESPONSE_MAP = Map.of(
            CMD_DO, CMD_WILL,
            CMD_DONT, CMD_WONT,
            CMD_WILL, CMD_DO,
            CMD_WONT, CMD_DONT);

    public static final Map<Integer, Integer> REJECT_RESPONSE_MAP = Map.of(
            CMD_DO, CMD_DONT,
            CMD_DONT, CMD_DO,
            CMD_WILL, CMD_WONT,
            CMD_WONT, CMD_WILL);

    public static final Map<Integer, Integer> OPTION_ALLOWED_MAP = Map.of(
            OPTION_MXP0, 1,
            OPTION_MXP1, 1);

    @Autowired
    private MudClient mudClient;

    @Override
    public boolean processMessage(InbMessage msg) {
        if (msg == null || !(msg instanceof IACConfirmInbMsg)) {
            return true; // Not an IAC confirm message, ignore
        }

        IACConfirmInbMsg iacMsg = (IACConfirmInbMsg) msg;
        if (iacMsg.getContentBytes() == null || iacMsg.getContentBytes().length < 3) {
            return true;
        }

        logger.info("服务器发送确认指令:" + String.format("IAC %s %d",
                CMD_NAME_MAP.getOrDefault(iacMsg.getContentBytes()[1], "UNKNOWN"), iacMsg.getContentBytes()[2]));

        int cmd = iacMsg.getContentBytes()[1];
        int opt = iacMsg.getContentBytes()[2];

        int isEnable = OPTION_ALLOWED_MAP.getOrDefault(opt, 0);
        int responseCmd = -1;
        int responseOpt = opt;
        if (isEnable == 1) {
            responseCmd = ACCEPT_RESPONSE_MAP.getOrDefault(cmd, -1);
        } else {
            responseCmd = REJECT_RESPONSE_MAP.getOrDefault(cmd, -1);
        }

        if (responseCmd != -1) {
            byte[] response = new byte[] {
                    (byte) 255, (byte) responseCmd, (byte) responseOpt
            };
            this.mudClient.send(response);
            logger.info("发送响应指令:"
                    + String.format("IAC %s %d", CMD_NAME_MAP.getOrDefault(responseCmd, "UNKNOWN"), responseOpt));
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 2;
    }

}

package zm.mud.network.inbound.processor;

import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.inbound.consts.IACConsts;
import zm.mud.network.inbound.message.IACConfirmInbMsg;
import zm.mud.network.inbound.message.InbMessage;

@Service
public class IACConfirmProcessor implements InbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(IACConfirmProcessor.class);

   

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
                IACConsts.CMD_NAME_MAP.getOrDefault(iacMsg.getContentBytes()[1], "UNKNOWN"), iacMsg.getContentBytes()[2]));

        int cmd = iacMsg.getContentBytes()[1];
        int opt = iacMsg.getContentBytes()[2];

        int isEnable = IACConsts.OPTION_ALLOWED_MAP.getOrDefault(opt, 0);
        int responseCmd = -1;
        int responseOpt = opt;
        if (isEnable == 1) {
            responseCmd = IACConsts.ACCEPT_RESPONSE_MAP.getOrDefault(cmd, -1);
        } else {
            responseCmd = IACConsts.REJECT_RESPONSE_MAP.getOrDefault(cmd, -1);
        }

        if (responseCmd != -1) {
            byte[] response = new byte[] {
                    (byte) 255, (byte) responseCmd, (byte) responseOpt
            };
            this.mudClient.send(response);
            logger.info("发送响应指令:"
                    + String.format("IAC %s %d", IACConsts.CMD_NAME_MAP.getOrDefault(responseCmd, "UNKNOWN"), responseOpt));
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 2;
    }

}

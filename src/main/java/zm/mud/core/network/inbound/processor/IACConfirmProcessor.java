package zm.mud.core.network.inbound.processor;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.client.MudClient;
import zm.mud.core.network.inbound.message.IACConfirmInbMsg;
import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.protocol.iac.handler.IIACCommandHandler;
import zm.mud.core.protocol.iac.sbhandler.IIACSBCommandHandler;
import zm.mud.utils.HexUtil;
import zm.mud.utils.SpringBeanUtil;

@Service
public class IACConfirmProcessor implements IInbMsgProcessor, Ordered {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(IACConfirmProcessor.class);

    @Autowired
    private MudClient mudClient;

    @Autowired
    private HexUtil hexUtil;

    @Override
    public boolean processMessage(InbMsg msg) {
        if (msg == null || !(msg instanceof IACConfirmInbMsg)) {
            return true; // Not an IAC confirm message, ignore
        }

        IACConfirmInbMsg iacMsg = (IACConfirmInbMsg) msg;
        if (iacMsg.getContentBytes() == null || iacMsg.getContentBytes().length < 3) {
            return true;
        }

        logger.debug("收到服务器指令：" + Arrays.toString(this.hexUtil.toHex(iacMsg.getContentBytes())));

        byte[] response = null;
        if (!this.isSBCommand(iacMsg.getContentBytes())) {
            response = this.handleIAC(iacMsg.getContentBytes());
        } else {
            response = this.handleIACSub(iacMsg.getContentBytes());
        }
        if (response == null) {
            logger.debug("不支持当前IAC指令或者不响应：" + Arrays.toString(this.hexUtil.toHex(iacMsg.getContentBytes())));
            return true;
        }

        logger.debug("发送响应指令" + Arrays.toString(this.hexUtil.toHex(response)));
        this.mudClient.send(response);

        return true;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    private byte[] handleIAC(byte[] iacCommand) {
        byte optionCode = iacCommand[2];
        String beanId = IACConsts.IAC_HANDLER_BEAN_PREFIX + this.hexUtil.toHex(optionCode);
        IIACCommandHandler handler = SpringBeanUtil.getBean(beanId, IIACCommandHandler.class);
        if (handler == null) {
            logger.debug("No special handler found. Process with common handler:" + beanId);
            handler = SpringBeanUtil.getBean(IACConsts.IAC_HANDLER_COMMON, IIACCommandHandler.class);
        }

        return handler.handle(iacCommand);
    }

    private byte[] handleIACSub(byte[] iacCommand) {
        byte optionCode = iacCommand[2];
        String beanId = IACConsts.IAC_SB_HANDLER_BEAN_PREFIX + this.hexUtil.toHex(optionCode);
        IIACSBCommandHandler handler = SpringBeanUtil.getBean(beanId, IIACSBCommandHandler.class);
        if (handler == null) {
            logger.warn("[Process as Default] Unsupport IAC Command:" + beanId);
            handler = SpringBeanUtil.getBean(IACConsts.IAC_SB_HANDLER_DEFAULT, IIACSBCommandHandler.class);
        }

        return handler.handle(iacCommand);
    }

    // SB format: [FF, FA, xx,xx,..., FF, SE]
    private boolean isSBCommand(byte[] iacCommand) {
        return IACConsts.CMD_SB == (iacCommand[1] & 0xFF);
    }

}

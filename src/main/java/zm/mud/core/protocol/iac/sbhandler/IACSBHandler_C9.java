package zm.mud.core.protocol.iac.sbhandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zm.mud.core.cfg.ApplicationConfig;
import zm.mud.core.protocol.iac.consts.IACConsts;

import java.nio.charset.Charset;

/**
 * IAC GMCP (服务器发来的 GMCP 数据包)
 * 格式: [IAC] [SB] [GMCP] 包名 + 空格 + JSON数据 [IAC] [SE]
 */
@Component(IACConsts.IAC_SB_HANDLER_BEAN_PREFIX + "C9")
public class IACSBHandler_C9 implements IIACSBCommandHandler {

    private static final Logger logger = LogManager.getLogger(IACSBHandler_C9.class);

    // 北侠服务器标准的字符集编码
    private static final Charset MUD_CHARSET = Charset.forName("GBK");

    @Autowired
    private ApplicationConfig appCfg;

    /**
     * 处理服务器发来的 GMCP 消息
     * 
     * @param iacSubCommand 包含完整头尾的子协商原始字节数组
     * @return 客户端需要回应的字节，不需要回应则返回 null
     */
    @Override
    public byte[] handle(byte[] iacSubCommand) {
        if (iacSubCommand == null || iacSubCommand.length < 5) {
            return null;
        }

        try {
            int payloadLength = iacSubCommand.length - 5;
            if (payloadLength <= 0)
                return null;

            String rawMessage = new String(iacSubCommand, 3, payloadLength, MUD_CHARSET);
            int spaceIndex = rawMessage.indexOf(' ');

            String packageName = (spaceIndex == -1) ? rawMessage.trim() : rawMessage.substring(0, spaceIndex).trim();
            String jsonPayload = (spaceIndex == -1) ? "" : rawMessage.substring(spaceIndex + 1).trim();

            logger.info("【GMCP 消息】模块: {} | 数据: {}", packageName, jsonPayload);

            // ================== 【核心唤醒逻辑】 ==================
            // 如果收到了系统包（代表服务器开启了 GMCP 通道）
            if ("GMCP.System".equalsIgnoreCase(packageName)) {
                logger.info("【GMCP】检测到系统广播，发送最极简的 Core.Hello 唤醒报文...");

            }else{
                 logger.info("【GMCP】packageName: {}, jsonPayload: {}", packageName, jsonPayload);
            }

            // ====================================================

        } catch (Exception e) {
            logger.error("解析 GMCP 报文时发生异常", e);
        }

        return null;
    }

}

package zm.mud.core.protocol.iac.sbhandler;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import zm.mud.core.cfg.ApplicationConfig;
import zm.mud.core.protocol.iac.consts.IACConsts;
/**
 * IAC TERMINAL-TYPE (服务器要求你通报终端类型)
 */
@Component(IACConsts.IAC_SB_HANDLER_BEAN_PREFIX + "18")
public class IACSBHandler_18 implements IIACSBCommandHandler{

    @Autowired
    private ApplicationConfig appCfg;

      /**
     * 处理服务器发来的 Terminal-Type 子协商请求
     * 
     * @param iacSubCommand 传入你收到的字节流,例如 [FF, FA, 18, 01, FF, F0]
     * @return 客户端必须回应的 9 字节终端名报文
     */
    @Override
    public List<byte[]> handle(byte[] iacSubCommand) {
         if (iacSubCommand == null || iacSubCommand.length < 3) {
            return null;
        }

        // 🔍 智能寻找 SEND (01) 指令的位置，兼容各种不同的拆包边界
        boolean isSendRequest = false;
        for (int i = 0; i < iacSubCommand.length; i++) {
            // 如果发现 18 后面紧跟着 01，说明就是服务器在向我们要终端名字
            if ((iacSubCommand[i] & 0xFF) == 0x18 && (i + 1 < iacSubCommand.length) && iacSubCommand[i + 1] == 0x01) {
                isSendRequest = true;
                break;
            }
        }

        // 如果确定是服务器索要名字的请求
        if (isSendRequest) {
            // 定义客户端模拟的终端类型名（必须是标准的大写 ASCII 字符串，如 VT100, XTERM, ANSI）
            String terminalType = appCfg.getTerminalType() + " " + appCfg.getVersion(); 
            byte[] typeBytes = terminalType.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            
            // 标准返回格式：IAC (FF) + SB (FA) + OPTION (18) + IS (00) + [终端名] + IAC (FF) + SE (F0)
            byte[] response = new byte[6 + typeBytes.length];
            
            response[0] = (byte) IACConsts.IAC; // IAC
            response[1] = (byte) IACConsts.CMD_SB; // SB (子协商开始)
            response[2] = (byte) 0x18; // 选项码 18
            response[3] = (byte) 0x00; // IS (00，代表“这就是我的名字”)
            
            // 将 "VT100" 的字节拷贝到数组中间
            System.arraycopy(typeBytes, 0, response, 4, typeBytes.length);
            
            // 封尾
            response[response.length - 2] = (byte) IACConsts.IAC; // IAC
            response[response.length - 1] = (byte) IACConsts.CMD_SE; // SE (子协商结束)
            
            return Arrays.asList(response); 
        }
        
        return null;
    }
    
}

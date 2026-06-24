package zm.mud.core.protocol.iac.consts;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * IAC format: IAC <command> <option>
 * For subnegotiation: IAC CMD_SB <option> <data> IAC CMD_SE
 * For 3 bytes IAC commands format: IAC <command> <option>
 * For 2 bytes IAC commands format: IAC <command>
 */
public class IACConsts {

        
        /*
                1. format: IAC Handler Bean id : IAC_HANDLER_{option code}
                2. IAC_HANDLER_0X5A , upper case
         */
        public static final String IAC_HANDLER_BEAN_PREFIX = "IAC_HANDLER_"; 
        public static final String IAC_HANDLER_COMMON = "IAC_HANDLER_COMMON";

        
        public static final String IAC_SB_HANDLER_BEAN_PREFIX = "IAC_SB_HANDLER_"; 
        public static final String IAC_SB_HANDLER_DEFAULT = "IAC_SB_HANDLER_DEFAULT";

        public static final int IAC = 0xFF; // Interpret As Command

        public static final int CMD_SE = 0xF0; // subnegotiation end
        public static final int CMD_NOP = 0xF1;
        public static final int CMD_DM = 0xF2;
        public static final int CMD_BRK = 0xF3;
        public static final int CMD_IP = 0xF4;
        public static final int CMD_AO = 0xF5;
        public static final int CMD_AYT = 0xF6;
        public static final int CMD_EC = 0xF7;
        public static final int CMD_EL = 0xF8;
        public static final int CMD_GA = 0xF9; // go ahead
        public static final int CMD_SB = 0xFA; // subnegotiation begin

        public static final Set<Integer> NON_OPTION_COMMANDS = Set.of(
                        CMD_SE, CMD_NOP, CMD_DM, CMD_BRK, CMD_IP, CMD_AO, CMD_AYT, CMD_EC, CMD_EL, CMD_GA);


        public static final int CMD_WILL = 0xFB;
        public static final int CMD_WONT = 0xFC;
        public static final int CMD_DO = 0xFD;
        public static final int CMD_DONT = 0xFE;
        

        // Option Codes
        public static final int OPTION_18       = 0x18; // TERMINAL-TYPE (服务器要求你通报终端类型)
        public static final int OPTION_1F       = 0x1F; // NAWS (服务器要求你通报窗口大小)
        public static final int OPTION_5A       = 0x5A; //START-TLS (服务器声明支持安全传输 TLS 加密)
        
        public static final Set<Integer> enabledOpsSet = new HashSet<>();
        static{
                enabledOpsSet.add(OPTION_18);
        }


        public static final Map<Integer,Integer> rejectCmdMap = new HashMap<>();
        static{
                rejectCmdMap.put(CMD_WILL,CMD_WONT);
                rejectCmdMap.put(CMD_DO,CMD_DONT);
        }

        public static final Map<Integer,Integer> enableCmdMap = new HashMap<>();
        static{
                enableCmdMap.put(CMD_WILL,CMD_DO);
                enableCmdMap.put(CMD_DO,CMD_WILL);
        }

      
        
        private IACConsts() {
                // private constructor to prevent instantiation
        }
}

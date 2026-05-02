package zm.mud.network.inbound.consts;

import java.util.Map;
import java.util.Set;

/**
 * IAC format: IAC <command> <option>
 * For subnegotiation: IAC CMD_SB <option> <data> IAC CMD_SE
 * For 3 bytes IAC commands format: IAC <command> <option>
 * For 2 bytes IAC commands format: IAC <command>
 */
public class IACConsts {
        public static final int IAC = 255; // Interpret As Command
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
                        OPTION_MXP1, 1,
                1,1);

        private IACConsts() {
                // private constructor to prevent instantiation
        }
}

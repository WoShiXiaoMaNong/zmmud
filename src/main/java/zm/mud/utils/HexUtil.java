package zm.mud.utils;

import org.springframework.stereotype.Component;

@Component
public class HexUtil {

    // 16进制字符表
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public String[] toHex(byte[] msg) {
        if (msg == null || msg.length == 0) {
            return new String[0];
        }

        String[] hexArray = new String[msg.length];
        for (int i = 0; i < msg.length; i++) {
            hexArray[i] = this.toHex(msg[i]);
        }
        return hexArray;
    }

    public String toHex(byte msg) {
        int v = msg & 0xFF; // 将有符号 byte 转换为 0-255 的无符号正数
        char[] chars = new char[2];
        chars[0] = HEX_CHARS[v >>> 4]; // 获取高 4 位
        chars[1] = HEX_CHARS[v & 0x0F]; // 获取低 4 位
        return new String(chars);
    }
}

package zm.mud.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectionReversionUtil {

    // 声明不可变的全局静态 Map
    private static final Map<String, String> REVERT_MAP;

    static {
        Map<String, String> map = new HashMap<>();

        // ==========================================
        // 1. 基本四方位 (Primary Cardinal Directions)
        // ==========================================
        // 简写
        putBidirectional(map, "n", "s");
        putBidirectional(map, "e", "w");
        // 全写
        putBidirectional(map, "north", "south");
        putBidirectional(map, "east", "west");

        // ==========================================
        // 2. 四个间方位 (Ordinal / Intercardinal)
        // ==========================================
        // 简写 (注意：国际标准通常是东北NE，西南SW)
        putBidirectional(map, "ne", "sw");
        putBidirectional(map, "nw", "se");
        // 全写
        putBidirectional(map, "northeast", "southwest");
        putBidirectional(map, "northwest", "southeast");

        // ==========================================
        // 3. 垂直方位 (Vertical Directions)
        // ==========================================
        // 简写
        putBidirectional(map, "u", "d");
        // 全写
        putBidirectional(map, "up", "down");

        // ==========================================
        // 4. 三维复合方位 (3D Combined - 选填，游戏/建模常用)
        // ==========================================
        // 简写
        putBidirectional(map, "nu", "sd"); // 北上 <-> 南下
        putBidirectional(map, "su", "nd"); // 南上 <-> 北下
        putBidirectional(map, "eu", "wd"); // 东上 <-> 西下
        putBidirectional(map, "wu", "ed"); // 西上 <-> 东下

        putBidirectional(map, "neu", "swd"); // 东北上 <-> 西南下
        putBidirectional(map, "nwu", "sed"); // 西北上 <-> 东南下
        putBidirectional(map, "seu", "nwd"); // 东南上 <-> 西北下
        putBidirectional(map, "swu", "ned"); // 西南上 <-> 东北下

        // 全写
        putBidirectional(map, "northup", "southdown");
        putBidirectional(map, "southup", "northdown");
        putBidirectional(map, "eastup", "westdown");
        putBidirectional(map, "westup", "eastdown");

        putBidirectional(map, "northeastup", "southwestdown");
        putBidirectional(map, "northwestup", "southeastdown");
        putBidirectional(map, "southeastup", "northwestdown");
        putBidirectional(map, "southwestup", "northeastdown");

        // ==========================================
        // 5. 特殊动作/原地方位 (Special / Static)
        // ==========================================
        putBidirectional(map, "wa", "wa"); // Wait / Stay 原地不变
        putBidirectional(map, "wait", "wait");

        // 使用 unmodifiableMap 确保多线程安全
        REVERT_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 辅助方法：同时存入正反双向的映射
     */
    private static void putBidirectional(Map<String, String> map, String dir1, String dir2) {
        map.put(dir1.toLowerCase(), dir2.toLowerCase());
        map.put(dir2.toLowerCase(), dir1.toLowerCase());
    }

    /**
     * 获取反转后的方向
     * 
     * @param direction 输入的方向字符串（自动处理空格、大小写）
     * @return 反转后的全小写方向；若未匹配到则返回 null
     */
    public static String revert(String direction) {
        if (direction == null) {
            return null;
        }
        // 去除前后空格并转为小写
        String cleaned = direction.trim().toLowerCase();
        return REVERT_MAP.get(cleaned);
    }
}
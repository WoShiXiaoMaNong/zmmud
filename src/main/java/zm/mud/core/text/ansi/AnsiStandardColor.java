package zm.mud.core.text.ansi;

/**
 * 标准 ANSI 16色及核心默认底色的语义化枚举
 * 只用于现在名称获取，并不参与实际的文字颜色渲染。
 */
public enum AnsiStandardColor {
    // --- 核心默认底色 ---
    DEFAULT_FG(0xE5E5E5), // 默认前景色 (标准白)
    DEFAULT_BG(0x000000), // 默认背景色 (标准黑)

    // --- 标准 8 色 (索引 0-7) ---
    BLACK(0x000000),
    RED(0xCD0000),
    GREEN(0x00CD00),
    YELLOW(0xCDCD00),
    BLUE(0x0000EE),
    MAGENTA(0xCD00CD),
    CYAN(0x00CDCD),
    WHITE(0xE5E5E5),

    // --- 高亮/暗色 8 色 (索引 8-15) ---
    BRIGHT_BLACK(0x7F7F7F),
    BRIGHT_RED(0xFF0000),
    BRIGHT_GREEN(0x00FF00),
    BRIGHT_YELLOW(0xFFFF00),
    BRIGHT_BLUE(0x5C5CFF),
    BRIGHT_MAGENTA(0xFF00FF),
    BRIGHT_CYAN(0x00FFFF),
    BRIGHT_WHITE(0xFFFFFF);

    private final int rgb;

    AnsiStandardColor(int rgb) {
        this.rgb = rgb;
    }

    public int getRgb() {
        return this.rgb;
    }

    // 按标准 ANSI 0-15 索引排列的快速映射数组
    private static final AnsiStandardColor[] INDEX_MAPPING = {
        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
        BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW, BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE
    };

    /**
     * 根据 0-15 的标准 ANSI 颜色索引获取枚举项
     */
    public static AnsiStandardColor fromIndex(int index) {
        if (index >= 0 && index < INDEX_MAPPING.length) {
            return INDEX_MAPPING[index];
        }
        return DEFAULT_FG;
    }

    /**
     * 根据显示名称（Display Name）不区分大小写获取枚举项
     */
    public static AnsiStandardColor fromName(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return AnsiStandardColor.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

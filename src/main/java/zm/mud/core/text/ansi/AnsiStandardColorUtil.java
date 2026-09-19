package zm.mud.core.text.ansi;

public class AnsiStandardColorUtil {

    /**
     * 外部可通过 Display Name 直接获取标准 RGB 值
     */
    public static Integer getColorByName(String displayName) {
        AnsiStandardColor color = AnsiStandardColor.fromName(displayName);
        return color != null ? color.getRgb() : null;
    }

    /**
     * 将原始 ANSI 编码字符串转换为标准 24位整型 RGB 值 (0xRRGGBB)
     */
    public static int convertAnsiToIntRgb(String ansiCode, boolean isBackground) {
        if (ansiCode == null || ansiCode.trim().isEmpty() || "default".equalsIgnoreCase(ansiCode.trim())) {
            return isBackground ? AnsiStandardColor.DEFAULT_BG.getRgb() : AnsiStandardColor.DEFAULT_FG.getRgb();
        }

        String trimmed = ansiCode.trim();

        // 1. 处理扩展 256 色
        if (trimmed.contains(";")) {
            String[] parts = trimmed.split(";");
            if (parts.length >= 3 && "5".equals(parts[1].trim())) {
                try {
                    int index = Integer.parseInt(parts[2].trim());
                    return getAnsi256Rgb(index);
                } catch (NumberFormatException e) {
                    return isBackground ? AnsiStandardColor.DEFAULT_BG.getRgb() : AnsiStandardColor.DEFAULT_FG.getRgb();
                }
            }
        }

        // 2. 处理标准 8/16 基本色
        try {
            int code = Integer.parseInt(trimmed);
            int finalIndex = -1;

            if (!isBackground) {
                if (code >= 30 && code <= 37) finalIndex = code - 30;
                else if (code >= 90 && code <= 97) finalIndex = code - 90 + 8;
            } else {
                if (code >= 40 && code <= 47) finalIndex = code - 40;
                else if (code >= 100 && code <= 107) finalIndex = code - 100 + 8;
            }

            if (finalIndex >= 0 && finalIndex < 16) {
                return AnsiStandardColor.fromIndex(finalIndex).getRgb();
            }
        } catch (NumberFormatException ignored) {}

        return isBackground ? AnsiStandardColor.DEFAULT_BG.getRgb() : AnsiStandardColor.DEFAULT_FG.getRgb();
    }

    /**
     * 通过原始 ANSI 编码字符串，反向解析获取它的 Display Name
     */
    public static String getAnsiDisplayName(String ansiCode, boolean isBackground) {
        if (ansiCode == null || ansiCode.trim().isEmpty() || "default".equalsIgnoreCase(ansiCode.trim())) {
            return isBackground ? AnsiStandardColor.DEFAULT_BG.name() : AnsiStandardColor.DEFAULT_FG.name();
        }

        String trimmed = ansiCode.trim();

        // 1. 处理扩展 256 色
        if (trimmed.contains(";")) {
            String[] parts = trimmed.split(";");
            if (parts.length >= 3 && "5".equals(parts[1].trim())) {
                try {
                    int index = Integer.parseInt(parts[2].trim());
                    if (index < 16) {
                        return AnsiStandardColor.fromIndex(index).name();
                    }
                    return "ANSI_256_" + index;
                } catch (NumberFormatException e) {
                    return isBackground ? AnsiStandardColor.DEFAULT_BG.name() : AnsiStandardColor.DEFAULT_FG.name();
                }
            }
        }

        // 2. 处理标准 8/16 基本色
        try {
            int code = Integer.parseInt(trimmed);
            int finalIndex = -1;

            if (!isBackground) {
                if (code >= 30 && code <= 37) finalIndex = code - 30;
                else if (code >= 90 && code <= 97) finalIndex = code - 90 + 8;
            } else {
                if (code >= 40 && code <= 47) finalIndex = code - 40;
                else if (code >= 100 && code <= 107) finalIndex = code - 100 + 8;
            }

            if (finalIndex >= 0 && finalIndex < 16) {
                return AnsiStandardColor.fromIndex(finalIndex).name();
            }
        } catch (NumberFormatException ignored) {}

        return "UNKNOWN";
    }

    /**
     * 依据 ANSI 扩展 256 色的标准公式计算 RGB 数字
     */
    private static int getAnsi256Rgb(int index) {
        if (index < 0 || index > 255) return AnsiStandardColor.DEFAULT_FG.getRgb();

        // 0-15: 直接复用枚举映射
        if (index < 16) {
            return AnsiStandardColor.fromIndex(index).getRgb();
        }

        // 16-231: 6x6x6 颜色立方体
        if (index <= 231) {
            int idx = index - 16;
            int r = (idx / 36) % 6;
            int g = (idx / 6) % 6;
            int b = idx % 6;
            
            int red   = r == 0 ? 0 : 55 + r * 40;
            int green = g == 0 ? 0 : 55 + g * 40;
            int blue  = b == 0 ? 0 : 55 + b * 40;
            
            return (red << 16) | (green << 8) | blue;
        }

        // 232-255: 24 阶灰度级
        int graySteps = index - 232;
        int gray = 8 + graySteps * 10;
        return (gray << 16) | (gray << 8) | gray;
    }
}

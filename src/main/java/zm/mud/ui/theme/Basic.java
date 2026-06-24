package zm.mud.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

public class Basic implements ITheme {
    public static final Basic INSTANCE = new Basic();

    private static final Map<String, Color> ANSI_FOREGROUND_MAP = Map.ofEntries(
            // 普通前景色
            Map.entry("30", new Color(0, 0, 0)), // 黑
            Map.entry("31", new Color(205, 0, 0)), // 红
            Map.entry("32", new Color(0, 205, 0)), // 绿
            Map.entry("33", new Color(205, 205, 0)), // 黄
            Map.entry("34", new Color(0, 0, 238)), // 蓝
            Map.entry("35", new Color(205, 0, 205)), // 品红 / 紫
            Map.entry("36", new Color(0, 205, 205)), // 青
            // Map.entry("37", new Color(229, 229, 229)), // 白（浅灰）
            Map.entry("37", new Color(117, 117, 117)), // 白（浅灰）

            // 高亮前景色 (Bright)
            Map.entry("90", new Color(127, 127, 127)), // 亮黑 / 深灰
            Map.entry("91", new Color(255, 0, 0)), // 亮红
            Map.entry("92", new Color(0, 255, 0)), // 亮绿
            Map.entry("93", new Color(255, 255, 0)), // 亮黄
            Map.entry("94", new Color(92, 92, 255)), // 亮蓝
            Map.entry("95", new Color(255, 0, 255)), // 亮品红
            Map.entry("96", new Color(0, 255, 255)), // 亮青
            Map.entry("97", new Color(255, 255, 255)) // 亮白
    );

    private static final Map<String, Color> ANSI_BACKGROUND_MAP = Map.ofEntries(
            Map.entry("40", new Color(0, 0, 0)), // 黑
            Map.entry("41", new Color(205, 0, 0)), // 红
            Map.entry("42", new Color(0, 205, 0)), // 绿
            Map.entry("43", new Color(205, 205, 0)), // 黄
            Map.entry("44", new Color(0, 0, 238)), // 蓝
            Map.entry("45", new Color(205, 0, 205)), // 品红 / 紫
            Map.entry("46", new Color(0, 205, 205)), // 青
            Map.entry("47", new Color(229, 229, 229)), // 白（浅灰）
            Map.entry("100", new Color(127, 127, 127)), // 高亮黑 / 深灰
            Map.entry("101", new Color(255, 0, 0)), // 高亮红
            Map.entry("102", new Color(0, 255, 0)), // 高亮绿
            Map.entry("103", new Color(255, 255, 0)), // 高亮黄
            Map.entry("104", new Color(92, 92, 255)), // 高亮蓝
            Map.entry("105", new Color(255, 0, 255)), // 高亮品红
            Map.entry("106", new Color(0, 255, 255)), // 高亮青
            Map.entry("107", new Color(255, 255, 255)) // 高亮白
    );

    private static final Color BACKGROUD_COLOR = ANSI_BACKGROUND_MAP.get("107");
    private static final Color FOREGROUND_COLOR = ANSI_FOREGROUND_MAP.get("30");

    private Basic() {

    }

    @Override
    public Color getForeground(String code) {
        return ANSI_FOREGROUND_MAP.get(code);
    }

    @Override
    public Color getBackground(String code) {
        return ANSI_BACKGROUND_MAP.get(code);
    }

    @Override
    public Font geFont() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'geFont'");
    }

    @Override
    public boolean isForegroundCode(String code) {
        return ANSI_FOREGROUND_MAP.containsKey(code);
    }

    @Override
    public boolean isBackground(String code) {
        return ANSI_BACKGROUND_MAP.containsKey(code);
    }

    public Color ansi256ToColor(int index) {
        if (index < 16) {
            // Standard + bright colors (reuse your maps)
            switch (index) {
                case 0:
                    return new Color(0, 0, 0);
                case 1:
                    return new Color(205, 0, 0);
                case 2:
                    return new Color(0, 205, 0);
                case 3:
                    return new Color(205, 205, 0);
                case 4:
                    return new Color(0, 0, 238);
                case 5:
                    return new Color(205, 0, 205);
                case 6:
                    return new Color(0, 205, 205);
                case 7:
                    return new Color(229, 229, 229);
                case 8:
                    return new Color(127, 127, 127);
                case 9:
                    return new Color(255, 0, 0);
                case 10:
                    return new Color(0, 255, 0);
                case 11:
                    return new Color(255, 255, 0);
                case 12:
                    return new Color(92, 92, 255);
                case 13:
                    return new Color(255, 0, 255);
                case 14:
                    return new Color(0, 255, 255);
                case 15:
                    return new Color(255, 255, 255);
                default:
                    return Color.BLACK;
            }
        } else if (index >= 16 && index <= 231) {
            // 6x6x6 color cube
            int idx = index - 16;
            int r = (idx / 36) % 6;
            int g = (idx / 6) % 6;
            int b = idx % 6;
            return new Color(r == 0 ? 0 : 55 + r * 40,
                    g == 0 ? 0 : 55 + g * 40,
                    b == 0 ? 0 : 55 + b * 40);
        } else if (index >= 232 && index <= 255) {
            // grayscale
            int gray = 8 + (index - 232) * 10;
            return new Color(gray, gray, gray);
        }
        return Color.BLACK; // fallback
    }

    @Override
    public Color getDefaultForeground() {
        return FOREGROUND_COLOR;
    }

    @Override
    public Color getDefaultBackground() {
        return BACKGROUD_COLOR;
    }

    @Override
    public Color dimColor(Color c) {
        // 如果太亮，就不调暗
        if (isLightColor(c))
            return c;
        int r = (int) (c.getRed() * 0.7);
        int g = (int) (c.getGreen() * 0.7);
        int b = (int) (c.getBlue() * 0.7);
        return new Color(r, g, b);
    }

    private boolean isLightColor(Color color) {
        // 亮度公式：0.299*R + 0.587*G + 0.114*B
        int brightness = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        return brightness > 200; // 可调阈值
    }

    @Override
    public Color ensureContrast(Color fg, Color bg) {

        while (getContrastRatio(fg, bg) < 4.5) {
            fg = (luminance(bg) < 0.5)
                    ? brightenStrong(fg)
                    : darkenStrong(fg);
        }
        return fg;
    }

    private double luminance(Color c) {
        return 0.2126 * c.getRed() / 255.0 +
                0.7152 * c.getGreen() / 255.0 +
                0.0722 * c.getBlue() / 255.0;
    }

    private double getContrastRatio(Color c1, Color c2) {
        double l1 = luminance(c1);
        double l2 = luminance(c2);
        double brighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (brighter + 0.05) / (darker + 0.05);
    }

    private Color brightenStrong(Color c) {
        return new Color(
                Math.min(255, c.getRed() + 120),
                Math.min(255, c.getGreen() + 120),
                Math.min(255, c.getBlue() + 120));
    }

    private Color darkenStrong(Color c) {
        return new Color(
                Math.max(0, c.getRed() - 120),
                Math.max(0, c.getGreen() - 120),
                Math.max(0, c.getBlue() - 120));
    }

}

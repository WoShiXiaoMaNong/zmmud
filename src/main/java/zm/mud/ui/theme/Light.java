package zm.mud.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

public class Light implements ITheme {
    public static final Light INSTANCE = new Light();

    // =========================
    // 前景色（白底优化版）
    // =========================
    private static final Map<String, Color> ANSI_FOREGROUND_MAP = Map.ofEntries(
            Map.entry("30", new Color(30, 30, 30)),     // 黑
            Map.entry("31", new Color(180, 0, 0)),      // 红
            Map.entry("32", new Color(0, 140, 0)),      // 绿
            Map.entry("33", new Color(150, 110, 0)),    // 黄（微调，使其在白底下更清晰）
            Map.entry("34", new Color(0, 0, 180)),      // 蓝
            Map.entry("35", new Color(160, 0, 160)),    // 品红
            Map.entry("36", new Color(0, 140, 140)),    // 青
            Map.entry("37", new Color(120, 120, 120)),  // 灰

            Map.entry("90", new Color(100, 100, 100)), // 深灰
            Map.entry("91", new Color(220, 50, 50)),
            Map.entry("92", new Color(30, 160, 30)),
            Map.entry("93", new Color(190, 150, 20)),
            Map.entry("94", new Color(60, 60, 200)),
            Map.entry("95", new Color(180, 50, 180)),
            Map.entry("96", new Color(30, 150, 150)),
            Map.entry("97", new Color(0, 0, 0))         // 高亮白 → 黑
    );

    // =========================
    // 背景色（白底优化）
    // =========================
    private static final Map<String, Color> ANSI_BACKGROUND_MAP = Map.ofEntries(
            Map.entry("40", new Color(230, 230, 230)), // 防穿帮：MUD黑背景在白底下映射为浅浅灰
            Map.entry("41", new Color(255, 210, 210)),
            Map.entry("42", new Color(210, 255, 210)),
            Map.entry("43", new Color(255, 245, 190)),
            Map.entry("44", new Color(210, 210, 255)),
            Map.entry("45", new Color(255, 210, 255)),
            Map.entry("46", new Color(210, 255, 255)),
            Map.entry("47", new Color(245, 245, 245)), // 主背景

            Map.entry("100", new Color(200, 200, 200)),
            Map.entry("101", new Color(255, 180, 180)),
            Map.entry("102", new Color(180, 255, 180)),
            Map.entry("103", new Color(255, 240, 150)),
            Map.entry("104", new Color(180, 180, 255)),
            Map.entry("105", new Color(255, 180, 255)),
            Map.entry("106", new Color(180, 255, 255)),
            Map.entry("107", new Color(255, 255, 255)) 
    );

    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); 
    private static final Color FOREGROUND_COLOR = new Color(30, 30, 30);   
    private static final double CONTRAST_THRESHOLD = 4.5;

    private Light() {}

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
        return new Font("Monospaced", Font.PLAIN, 14);
    }

    @Override
    public boolean isForegroundCode(String code) {
        return ANSI_FOREGROUND_MAP.containsKey(code);
    }

    @Override
    public boolean isBackground(String code) {
        return ANSI_BACKGROUND_MAP.containsKey(code);
    }

    @Override
    public Color getDefaultForeground() {
        return FOREGROUND_COLOR;
    }

    @Override
    public Color getDefaultBackground() {
        return BACKGROUND_COLOR;
    }

    @Override
    public Color ansi256ToColor(int index) {
        // 修复 8-15 映射错误
        if (index < 8) {
            return ANSI_FOREGROUND_MAP.getOrDefault(String.valueOf(30 + index), FOREGROUND_COLOR);
        } else if (index < 16) {
            return ANSI_FOREGROUND_MAP.getOrDefault(String.valueOf(90 + (index - 8)), FOREGROUND_COLOR);
        } else if (index <= 231) {
            int idx = index - 16;
            int r = (idx / 36) % 6;
            int g = (idx / 6) % 6;
            int b = idx % 6;
            return new Color(
                    r == 0 ? 0 : 55 + r * 40,
                    g == 0 ? 0 : 55 + g * 40,
                    b == 0 ? 0 : 55 + b * 40
            );
        } else {
            int gray = 8 + (index - 232) * 10;
            return new Color(gray, gray, gray);
        }
    }

    @Override
    public Color dimColor(Color c) {
        // 👉 修复：白底下的 dim 是让颜色变淡（向白色靠拢），而不是变暗！
        int r = (int) (c.getRed() + (255 - c.getRed()) * 0.4);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * 0.4);
        int b = (int) (c.getBlue() + (255 - c.getBlue()) * 0.4);
        return new Color(r, g, b);
    }

    @Override
    public Color ensureContrast(Color fg, Color bg) {
        if (fg == null || bg == null) return fg;

        double contrast = getContrastRatio(fg, bg);
        if (contrast >= CONTRAST_THRESHOLD) {
            return fg; 
        }

        // 步进式精确调整，最多微调 5 次，大幅减少对象创建，防止死循环
        boolean bgIsDark = luminance(bg) < 0.5;
        int r = fg.getRed();
        int g = fg.getGreen();
        int b = fg.getBlue();

        for (int i = 0; i < 5; i++) {
            if (bgIsDark) {
                // 背景暗，文字需要变亮
                r = Math.min(255, (int)(r * 1.2 + 10));
                g = Math.min(255, (int)(g * 1.2 + 10));
                b = Math.min(255, (int)(b * 1.2 + 10));
            } else {
                // 背景亮（白底常见），文字需要变暗
                r = (int)(r * 0.75);
                g = (int)(g * 0.75);
                b = (int)(b * 0.75);
            }
            Color checkColor = new Color(r, g, b);
            if (getContrastRatio(checkColor, bg) >= CONTRAST_THRESHOLD) {
                return checkColor;
            }
        }
        
        // 兜底策略
        return bgIsDark ? new Color(255, 255, 255) : new Color(30, 30, 30);
    }

    private double luminance(Color c) {
        return 0.2126 * (c.getRed() / 255.0) +
               0.7152 * (c.getGreen() / 255.0) +
               0.0722 * (c.getBlue() / 255.0);
    }

    private double getContrastRatio(Color c1, Color c2) {
        double l1 = luminance(c1);
        double l2 = luminance(c2);
        double brighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (brighter + 0.05) / (darker + 0.05);
    }
}

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
            // 普通色（降低亮度，提升可读性）
            Map.entry("30", new Color(30, 30, 30)),     // 黑（主文字）
            Map.entry("31", new Color(180, 0, 0)),      // 红（降低刺眼）
            Map.entry("32", new Color(0, 140, 0)),      // 绿（收敛）
            Map.entry("33", new Color(180, 140, 0)),    // 黄（关键！避免看不见）
            Map.entry("34", new Color(0, 0, 180)),      // 蓝（加深）
            Map.entry("35", new Color(160, 0, 160)),    // 品红
            Map.entry("36", new Color(0, 140, 140)),    // 青
            Map.entry("37", new Color(120, 120, 120)),  // 灰（辅助文本）

            // 高亮色（更清晰但不过曝）
            Map.entry("90", new Color(150, 150, 150)),
            Map.entry("91", new Color(220, 50, 50)),
            Map.entry("92", new Color(50, 180, 50)),
            Map.entry("93", new Color(220, 180, 50)),
            Map.entry("94", new Color(80, 80, 220)),
            Map.entry("95", new Color(200, 80, 200)),
            Map.entry("96", new Color(50, 180, 180)),
            Map.entry("97", new Color(0, 0, 0)) // 高亮白 → 黑（避免白底白字）
    );

    // =========================
    // 背景色（白底优化）
    // =========================
    private static final Map<String, Color> ANSI_BACKGROUND_MAP = Map.ofEntries(
            Map.entry("40", new Color(40, 40, 40)),
            Map.entry("41", new Color(255, 220, 220)),
            Map.entry("42", new Color(220, 255, 220)),
            Map.entry("43", new Color(255, 250, 200)),
            Map.entry("44", new Color(220, 220, 255)),
            Map.entry("45", new Color(255, 220, 255)),
            Map.entry("46", new Color(220, 255, 255)),
            Map.entry("47", new Color(245, 245, 245)), // 主背景（柔白）

            Map.entry("100", new Color(200, 200, 200)),
            Map.entry("101", new Color(255, 180, 180)),
            Map.entry("102", new Color(180, 255, 180)),
            Map.entry("103", new Color(255, 240, 150)),
            Map.entry("104", new Color(180, 180, 255)),
            Map.entry("105", new Color(255, 180, 255)),
            Map.entry("106", new Color(180, 255, 255)),
            Map.entry("107", new Color(255, 255, 255)) // 纯白（少用）
    );

    // =========================
    // 默认颜色
    // =========================
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // 柔白
    private static final Color FOREGROUND_COLOR = new Color(30, 30, 30);   // 深灰（护眼）

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

    // =========================
    // ANSI 256（保持不变）
    // =========================
    public Color ansi256ToColor(int index) {
        if (index < 16) {
            return ANSI_FOREGROUND_MAP.getOrDefault(String.valueOf(30 + index), FOREGROUND_COLOR);
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

    // =========================
    // dim（白底适当减弱）
    // =========================
    @Override
    public Color dimColor(Color c) {
        int r = (int) (c.getRed() * 0.8);
        int g = (int) (c.getGreen() * 0.8);
        int b = (int) (c.getBlue() * 0.8);
        return new Color(r, g, b);
    }

    // =========================
    // 对比度增强
    // =========================
    @Override
    public Color ensureContrast(Color fg, Color bg) {
        int guard = 0;
        while (getContrastRatio(fg, bg) < 4.5 && guard++ < 10) {
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
                Math.min(255, c.getRed() + 80),
                Math.min(255, c.getGreen() + 80),
                Math.min(255, c.getBlue() + 80)
        );
    }

    private Color darkenStrong(Color c) {
        return new Color(
                Math.max(0, c.getRed() - 80),
                Math.max(0, c.getGreen() - 80),
                Math.max(0, c.getBlue() - 80)
        );
    }
}
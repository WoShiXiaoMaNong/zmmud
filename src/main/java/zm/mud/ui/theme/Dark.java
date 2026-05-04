package zm.mud.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

public class Dark implements ITheme {
    public static final Dark INSTANCE = new Dark();

    // =========================
    // 前景色（针对黑底优化）
    // =========================
    private static final Map<String, Color> ANSI_FOREGROUND_MAP = Map.ofEntries(
            // 普通前景色（整体提亮一点）
            Map.entry("30", new Color(180, 180, 180)), // 黑 → 灰（否则看不见）
            Map.entry("31", new Color(255, 85, 85)),   // 红
            Map.entry("32", new Color(80, 200, 120)),  // 绿
            Map.entry("33", new Color(230, 200, 80)),  // 黄
            Map.entry("34", new Color(100, 150, 255)), // 蓝
            Map.entry("35", new Color(200, 120, 255)), // 品红
            Map.entry("36", new Color(80, 220, 220)),  // 青
            Map.entry("37", new Color(210, 210, 210)), // 白（主文本）

            // 高亮前景色（更亮更清晰）
            Map.entry("90", new Color(120, 120, 120)), // 深灰
            Map.entry("91", new Color(255, 120, 120)),
            Map.entry("92", new Color(120, 255, 120)),
            Map.entry("93", new Color(255, 255, 120)),
            Map.entry("94", new Color(140, 180, 255)),
            Map.entry("95", new Color(255, 140, 255)),
            Map.entry("96", new Color(120, 255, 255)),
            Map.entry("97", new Color(255, 255, 255))  // 纯白
    );

    // =========================
    // 背景色（黑底优化）
    // =========================
    private static final Map<String, Color> ANSI_BACKGROUND_MAP = Map.ofEntries(
            Map.entry("40", new Color(20, 20, 20)),   // 黑（默认背景）
            Map.entry("41", new Color(120, 40, 40)),
            Map.entry("42", new Color(40, 120, 40)),
            Map.entry("43", new Color(120, 120, 40)),
            Map.entry("44", new Color(40, 40, 120)),
            Map.entry("45", new Color(120, 40, 120)),
            Map.entry("46", new Color(40, 120, 120)),
            Map.entry("47", new Color(200, 200, 200)), // 亮背景（少用）

            Map.entry("100", new Color(60, 60, 60)),
            Map.entry("101", new Color(180, 60, 60)),
            Map.entry("102", new Color(60, 180, 60)),
            Map.entry("103", new Color(180, 180, 60)),
            Map.entry("104", new Color(60, 60, 180)),
            Map.entry("105", new Color(180, 60, 180)),
            Map.entry("106", new Color(60, 180, 180)),
            Map.entry("107", new Color(255, 255, 255))
    );

    // =========================
    // 默认前景/背景
    // =========================
    private static final Color BACKGROUND_COLOR = new Color(18, 18, 18); // 深黑（护眼）
    private static final Color FOREGROUND_COLOR = new Color(210, 210, 210); // 主文字
private static final double CONTRAST_THRESHOLD = 4.5; // WCAG 推荐
    private Dark() {}

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
    // ANSI 256（保持一致）
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
    // dim（黑底下不能太狠）
    // =========================
    @Override
    public Color dimColor(Color c) {
        int r = (int) (c.getRed() * 0.85);
        int g = (int) (c.getGreen() * 0.85);
        int b = (int) (c.getBlue() * 0.85);
        return new Color(r, g, b);
    }

    // =========================
    // 对比度增强（核心）
    // =========================
    @Override
   public Color ensureContrast(Color fg, Color bg) {
    if (fg == null || bg == null) return fg;

    double contrast = getContrastRatio(fg, bg);

    if (contrast >= CONTRAST_THRESHOLD) {
        return fg; // 已经足够清晰
    }

    boolean bgIsDark = luminance(bg) < 0.5;

    // 👉 关键：根据背景选择方向
    if (bgIsDark) {
        return brighten(fg, bg);
    } else {
        return darken(fg, bg);
    }
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

private Color brighten(Color fg, Color bg) {
    int r = fg.getRed();
    int g = fg.getGreen();
    int b = fg.getBlue();

    for (int i = 0; i < 5; i++) {
        r = Math.min(255, (int)(r * 1.2 + 10));
        g = Math.min(255, (int)(g * 1.2 + 10));
        b = Math.min(255, (int)(b * 1.2 + 10));

        Color c = new Color(r, g, b);
        if (getContrastRatio(c, bg) >= CONTRAST_THRESHOLD) {
            return c;
        }
    }
    return new Color(255, 255, 255);
}

private Color darken(Color fg, Color bg) {
    int r = fg.getRed();
    int g = fg.getGreen();
    int b = fg.getBlue();

    for (int i = 0; i < 5; i++) {
        r = (int)(r * 0.8);
        g = (int)(g * 0.8);
        b = (int)(b * 0.8);

        Color c = new Color(r, g, b);
        if (getContrastRatio(c, bg) >= CONTRAST_THRESHOLD) {
            return c;
        }
    }
    return new Color(30, 30, 30); // ❗ 不用纯黑，防刺眼
}
}
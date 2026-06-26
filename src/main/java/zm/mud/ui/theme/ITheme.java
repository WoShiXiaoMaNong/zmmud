package zm.mud.ui.theme;

import java.awt.Color;
import java.awt.Font;

import zm.mud.ui.cfg.ThemeType;

public interface ITheme {
    Color getDefaultForeground();
    Color getDefaultBackground();
    Color getForeground(String code);
    Color getBackground(String code);
    boolean isForegroundCode(String code);
    boolean isBackground(String code);
    Color ansi256ToColor(int index);
    Color dimColor(Color c);
    Color ensureContrast(Color fg, Color bg);
    Font geFont();

    /**
     * 将传入的颜色转换为高亮（或加粗替代）颜色
     * 注意，目的是为了有对比而更鲜艳，而不一定是提亮
     * @param color 当前的前景色
     * @return 调整后的高亮颜色
     */
    Color toBrighColor(Color color);
    
    default  Color resolveForeground(String code,Color bg){
        return getForeground(code);
    }

    public static ITheme getTheme(ThemeType type){
        if(ThemeType.BASIC == type){
            return Light.INSTANCE;
        }
        return Basic.INSTANCE; // Default
    }


    
        /**
     * 跨主题通用的高亮/鲜艳度调整方法
     * @param color 原始颜色
     * @param isDarkTheme 当前是否为暗黑主题
     * @return 调整后更具对比度、更鲜艳的颜色
     */
    default Color toUniversalBrightColor(Color color, boolean isDarkTheme) {
        if (color == null) return color;

        // 1. 将 RGB 转换为 HSB (色调, 饱和度, 亮度)
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];

        // 2. 提升饱和度，让颜色更“鲜艳” (最多提升到 1.0)
        saturation = Math.min(1.0f, saturation + 0.2f);

        // 3. 根据主题动态调整亮度，确保“高对比”
        if (isDarkTheme) {
            // 暗黑主题下：如果太暗，就大幅度提亮；如果本来就亮，小幅提亮
            brightness = (brightness < 0.5f) ? Math.min(1.0f, brightness + 0.4f) : Math.min(1.0f, brightness + 0.15f);
        } else {
            // 明亮主题下：如果太亮（看不清），就大幅加深；如果本来就深，保持或微调
            brightness = (brightness > 0.5f) ? Math.max(0.0f, brightness - 0.3f) : Math.max(0.0f, brightness - 0.1f);
        }

        // 4. 将 HSB 还原为标准的 RGB Color 对象（原生自带边界保护，绝不崩溃）
        return Color.getHSBColor(hue, saturation, brightness);
    }

}

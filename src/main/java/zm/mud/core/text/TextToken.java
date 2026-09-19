package zm.mud.core.text;

import zm.mud.core.text.ansi.AnsiStandardColorUtil;

/**
 * 文本样式 Token 节点
 */
public class TextToken {
    private final String text;
    private final boolean bold;
    private final boolean underline;

    // 原始 ANSI 颜色编码（如 "31", "45", "38;5;214", "default"）
    private final String rawForegroundCode;
    private final String rawBackgroundCode;

    // 经过 Theme 处理后的最终用于渲染的 RGB 数字
    private final int renderedFgRgb;
    private final int renderedBgRgb;

    public TextToken(String text, 
                     String rawForegroundCode, int renderedFgRgb, 
                     String rawBackgroundCode, int renderedBgRgb, 
                     boolean bold, boolean underline) {
        this.text = text;
        this.rawForegroundCode = rawForegroundCode;
        this.renderedFgRgb = renderedFgRgb;
        this.rawBackgroundCode = rawBackgroundCode;
        this.renderedBgRgb = renderedBgRgb;
        this.bold = bold;
        this.underline = underline;
    }

    public String getText() { return text; }
    public boolean isBold() { return bold; }
    public boolean isUnderline() { return underline; }
    
    public String getRawForegroundCode() { return rawForegroundCode; }
    public String getRawBackgroundCode() { return rawBackgroundCode; }
    
    public int getRenderedFgRgb() { return renderedFgRgb; }
    public int getRenderedBgRgb() { return renderedBgRgb; }

    @Override
    public String toString() {
        // 解析出前景色和背景色的语义化显示名称
        String fgName = AnsiStandardColorUtil.getAnsiDisplayName(rawForegroundCode, false);
        String bgName = AnsiStandardColorUtil.getAnsiDisplayName(rawBackgroundCode, true);

        return String.format(
            "Token{text='%s', fg(rawCode='%s', name='%s', renderRgb=%X), bg(rawCode='%s', name='%s', renderRgb=%X), bold=%b, underline=%b}", 
            text, rawForegroundCode, fgName, renderedFgRgb, rawBackgroundCode, bgName, renderedBgRgb, bold, underline
        );
    }
}

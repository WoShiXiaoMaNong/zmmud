package zm.mud.core.text.ansi;

import java.awt.Color;
import zm.mud.ui.theme.ITheme;


/**
 * 处理ansi颜色和 颜色重置不在同一行的情况
 * AnsiContext
 */
public class AnsiContext {
    private String lastRawFgCode = "default";
    private String lastRawBgCode = "default";
    private Color currentRenderedFg;
    private Color currentRenderedBg;
    private Color originalFgColor; // 用于背景色变化时，重新精准计算对比度
    private boolean isBold = false;
    private boolean isUnderline = false;

    public AnsiContext(ITheme theme) {
        reset(theme);
    }

    public void reset(ITheme theme) {
        this.lastRawFgCode = "default";
        this.lastRawBgCode = "default";
        this.currentRenderedFg = theme.getDefaultForeground();
        this.currentRenderedBg = theme.getDefaultBackground();
        this.originalFgColor = theme.getDefaultForeground();
        this.isBold = false;
        this.isUnderline = false;
    }

    public String getLastRawFgCode() { return lastRawFgCode; }
    public void setLastRawFgCode(String lastRawFgCode) { this.lastRawFgCode = lastRawFgCode; }

    public String getLastRawBgCode() { return lastRawBgCode; }
    public void setLastRawBgCode(String lastRawBgCode) { this.lastRawBgCode = lastRawBgCode; }

    public Color getCurrentRenderedFg() { return currentRenderedFg; }
    public void setCurrentRenderedFg(Color currentRenderedFg) { 
        this.currentRenderedFg = currentRenderedFg; 
    }

    public Color getCurrentRenderedBg() { return currentRenderedBg; }
    public void setCurrentRenderedBg(Color currentRenderedBg) { this.currentRenderedBg = currentRenderedBg; }

    public Color getOriginalFgColor() { return originalFgColor != null ? originalFgColor : currentRenderedFg; }
    public void setOriginalFgColor(Color originalFgColor) { this.originalFgColor = originalFgColor; }

    public boolean isBold() { return isBold; }
    public void setBold(boolean bold) { isBold = bold; }

    public boolean isUnderline() { return isUnderline; }
    public void setUnderline(boolean underline) { isUnderline = underline; }
}

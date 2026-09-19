package zm.mud.ui.component.menu.setting.client;

/**
 * 客户端配置项实体
 */
public class ClientConfigEntry {
    private String fontName = "Monospaced"; // 默认等宽字体
    private int fontSize = 16;              // 默认字号

    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }
}
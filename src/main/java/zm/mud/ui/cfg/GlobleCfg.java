package zm.mud.ui.cfg;

import java.awt.Font;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobleCfg {


    @Value("${mud.ui.size.width:800}")
    private int width;

    @Value("${mud.ui.size.height:600}")
    private int height;

    @Value("${mud.name:MUD Client}")
    private String title;

    @Value("${mud.server.name:}")
    private String serverName;

    @Value("${mud.ui.font.name:Monospaced}")
    private String fontName;

    @Value("${mud.ui.font.size:16}")
    private int fontSize;

    @Value("${mud.ui.theme:Basic}")
    private ThemeType themeType;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public ThemeType getThemeType() {
        return themeType;
    }

    public void setThemeType(ThemeType themeType) {
        this.themeType = themeType;
    }


    public Font getFont(){
        return new Font(this.getFontName(),Font.PLAIN,this.getFontSize());
    }

    

}

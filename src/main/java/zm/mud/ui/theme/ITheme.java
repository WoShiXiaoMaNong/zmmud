package zm.mud.ui.theme;

import java.awt.Color;
import java.awt.Font;

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
    
    default  Color resolveForeground(String code,Color bg){
        return getForeground(code);
    }

    public static ITheme getTheme(ThemeType type){
        if(ThemeType.BASIC == type){
            return Light.INSTANCE;
        }
        return Basic.INSTANCE; // Default
    }

}

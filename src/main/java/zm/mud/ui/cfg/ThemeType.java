package zm.mud.ui.cfg;

import zm.mud.ui.theme.Basic;
import zm.mud.ui.theme.Dark;
import zm.mud.ui.theme.ITheme;
import zm.mud.ui.theme.Light;

public enum ThemeType {
    BASIC(Basic.INSTANCE),
    DARK(Dark.INSTANCE),
    LIGHT(Light.INSTANCE)
    ;


    private ITheme theme;
    ThemeType(ITheme aTheme){
        this.theme = aTheme;
    }

    public ITheme getTheme(){
        return theme;
    }
}

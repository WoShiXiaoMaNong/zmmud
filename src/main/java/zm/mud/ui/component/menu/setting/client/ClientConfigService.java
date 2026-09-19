package zm.mud.ui.component.menu.setting.client;

import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.utils.SpringBeanUtil;

public class ClientConfigService {
    
    /**
     * 加载全局配置
     */
    public static ClientConfigEntry getGlobalConfig() {
        GlobalCfg cfg = SpringBeanUtil.getBean(GlobalCfg.class);
        ClientConfigEntry entry = new ClientConfigEntry();
        entry.setFontName("Sarasa Mono SC"); // 默认读取您注册的等宽字体
        entry.setFontSize(12);
        if( cfg != null && cfg.getFontName() != null){
            entry.setFontName(cfg.getFontName());
            entry.setFontSize(cfg.getFontSize());
        }
        return entry;
    }

    /**
     * 保存全局配置
     */
    public static void saveGlobalConfig(ClientConfigEntry entry) {
        if(entry == null){
            return;
        }
        
        GlobalCfg cfg = SpringBeanUtil.getBean(GlobalCfg.class);
        ZmMudUI ui = SpringBeanUtil.getBean(ZmMudUI.class);
        ui.resetFont(entry.getFontName(), entry.getFontSize());
        cfg.setFontName(entry.getFontName());
        cfg.setFontSize(entry.getFontSize());
    }
}
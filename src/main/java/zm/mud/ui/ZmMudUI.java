 package zm.mud.ui;

import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.ui.component.MudMainScreen;
import javax.swing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ZmMudUI {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZmMudUI.class);

    
    @Autowired
    private GlobleCfg globleCfg;

    private static ApplicationContext context;

    private MudMainScreen mudMain;


    @PostConstruct
    public void init(){
         mudMain = new MudMainScreen(globleCfg);
    }

  
    public void start() {
        SwingUtilities.invokeLater(() -> {
            mudMain.setShow();
            mudMain.resetFont(this.globleCfg.getFontName(), this.globleCfg.getFontSize());
        });
    }

   
    public void printlnToScreen(String text) {
        this.mudMain.printlnToScreen(text);
    }


    @Autowired
    public void setContext(ApplicationContext aContext){
        context = aContext;
    }

    public static ApplicationContext getContext(){
        return context;
    }

    // 解析 ANSI 并映射到 Swing 样式
   
}
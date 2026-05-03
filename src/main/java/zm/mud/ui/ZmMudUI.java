 package zm.mud.ui;




import zm.mud.ui.component.MudMainScreen;
import zm.mud.ui.component.MudTextAare;
import zm.mud.ui.util.AnsiToStyleDocUtil;

import javax.swing.*;
import javax.swing.text.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ZmMudUI {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZmMudUI.class);

    
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


   
    private static ApplicationContext context;

    private MudMainScreen mudMain;


    @PostConstruct
    public void init(){
         mudMain = new MudMainScreen(title + "( " + serverName + " )", width, height);
    }

  
    public void start() {
        SwingUtilities.invokeLater(() -> {
            mudMain.setShow();
            mudMain.resetFont(this.fontName, this.fontSize);
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
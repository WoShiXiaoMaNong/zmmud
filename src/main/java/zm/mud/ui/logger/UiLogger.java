package zm.mud.ui.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.IUiLogger;
import zm.mud.core.session.MudSession;
import zm.mud.ui.ZmMudUI;


@Service
public class UiLogger implements IUiLogger{

    // 采用标准的 MUD ANSI 格式 (\u001B[m)
    private static final String ANSI_RESET  = "\u001B[0m";   // 恢复默认颜色
    private static final String ANSI_GREEN  = "\u001B[32m";  // 绿色
    private static final String ANSI_YELLOW = "\u001B[33m"; // 黄色
    private static final String ANSI_RED    = "\u001B[31m";    // 红色
    
    // 如果想要颜色更加明亮（高亮/粗体），可以使用下面这组 1;3x 格式：
    // private static final String ANSI_GREEN_BRIGHT  = "\u001B[1;32m"; 
    // private static final String ANSI_YELLOW_BRIGHT = "\u001B[1;33m";
    // private static final String ANSI_RED_BRIGHT    = "\u001B[1;31m";

    @Autowired
    private ZmMudUI ui;

    @Override 
    public void info(MudSession session, String msg) {
        String coloredMsg = ANSI_GREEN + "【客户端消息】" + msg + ANSI_RESET;
        this.print(session, coloredMsg);
    }
    
    @Override 
    public void warn(MudSession session, String msg) {
        String coloredMsg = ANSI_YELLOW + "【客户端警告】" + msg + ANSI_RESET;
        this.print(session, coloredMsg);
    }
    
    @Override 
    public void error(MudSession session, String msg) {
        String coloredMsg = ANSI_RED + "【客户端错误】" + msg + ANSI_RESET;
        this.print(session, coloredMsg);
    }

    @Override
    public void print(MudSession session, String msg) {
        this.ui.printlnToScreen(session, msg);
    }
}

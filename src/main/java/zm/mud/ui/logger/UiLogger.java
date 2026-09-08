package zm.mud.ui.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.session.MudSession;
import zm.mud.ui.ZmMudUI;

@Service
public class UiLogger {

    @Autowired
    private ZmMudUI ui;

    public void info(MudSession session, String msg) {
        this.ui.printlnToScreen(session, "【客户端消息】" + msg);
    }

    public void warn(MudSession session, String msg) {
        this.ui.printlnToScreen(session, "【客户端警告】" +msg);
    }

    public void error(MudSession session, String msg) {
        this.ui.printlnToScreen(session, "【客户端错误】" +msg);
    }

}

package zm.mud.ui;

import zm.mud.core.api.InbMsgService;
import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.ui.component.MudMainScreen;
import zm.mud.ui.component.MudTextAare;
import zm.mud.ui.processor.MsgPrintProcessor;
import zm.mud.utils.FontUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ZmMudUI {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ZmMudUI.class);

    @Autowired
    private GlobleCfg globleCfg;

    @Autowired
    private MsgPrintProcessor msgPinter;

    private static ApplicationContext context;

    private MudMainScreen mudMain;

    @Autowired
    private InbMsgService inbMsgService;

    private static final ThreadPoolExecutor uiThreadPool = new ThreadPoolExecutor(
            1, 3, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1024),
            r -> {
                Thread t = new Thread(r, "fullme-thread");
                t.setDaemon(true); // 强烈建议：客户端退出时，这些线程会自动销毁
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    public void init() {
        mudMain = new MudMainScreen(globleCfg, this);
        FontUtil.registerFont();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            mudMain.setShow();
            mudMain.resetFont(this.globleCfg.getFontName(), this.globleCfg.getFontSize());
            inbMsgService.registerMsgHandler(msgPinter);
        });
    }

    public int getMsgOffset(String msg) {
        return this.mudMain.getMsgOffset(msg);
    }

    public void printlnToScreen(String text) {
        this.mudMain.printlnToScreen(text, false);
    }

    public void printlnToScreen(String text, boolean enableBlod) {
        this.mudMain.printlnToScreen(text, enableBlod);
    }

    /**
     * @see MudTextAare#printImg(String, int)
     * @param imgUrl
     * @param offset
     */
    public void printImg(String imgUrl, int offset, boolean insertMode) {
        uiThreadPool.execute(() -> {
            mudMain.printImg(imgUrl, offset, insertMode);
        });
    }

    @Autowired
    public void setContext(ApplicationContext aContext) {
        context = aContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

   
}
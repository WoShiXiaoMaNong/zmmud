package zm.mud.ui;

import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.MudMainScreen;
import zm.mud.ui.component.MudTextArea;
import zm.mud.ui.component.image.ImageDoubleClickListener;
import zm.mud.ui.component.image.ImageInfo;
import zm.mud.ui.component.image.MudImgIcon;
import zm.mud.utils.FontUtil;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import javax.swing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ZmMudUI {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ZmMudUI.class);

    private static final ImageDoubleClickListener IMG_DOUBLE_CLICK_LISTENER_POPUP = new ImageDoubleClickListener();

    @Autowired
    private GlobalCfg globleCfg;

    private static ApplicationContext context;

    private MudMainScreen mudMain;

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

    public void setTitle(MudSession session, String title) {
        this.mudMain.setTitle(session, title);
    }

    public void setCurrentUserName(MudSession session, String userName) {
        this.mudMain.setCurrentUserName(session, userName);
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            mudMain.setShow();
        });
    }

    public int getMsgOffset(MudSession session, String msg) {
        return this.mudMain.getMsgOffset(session, msg);
    }

    public void printlnToScreen(MudSession session, String text) {
        this.mudMain.printlnToScreen(session, text, false);
    }

    public void printlnToScreen(MudSession session, String text, boolean enableBlod) {
        this.mudMain.printlnToScreen(session, text, enableBlod);
    }

    /**
     * @see MudTextArea#printImg(String, int)
     * @param imgUrl
     * @param offset
     */
    public void printImg(MudSession session, List<ImageInfo> imgUrls, int offset) {
        this.printImg(session, imgUrls, offset, IMG_DOUBLE_CLICK_LISTENER_POPUP);
    }

    public void printImg(MudSession session, List<ImageInfo> imgUrls, int offset,
            BiConsumer<MouseEvent, MudImgIcon> onDoubleClick) {
        uiThreadPool.execute(() -> {
            mudMain.printImg(session, imgUrls, offset, onDoubleClick);
        });
    }

    public void printImg(MudSession session, List<ImageInfo> imgUrls) {
        this.printImg(session, imgUrls, IMG_DOUBLE_CLICK_LISTENER_POPUP);
    }

    public void printImg(MudSession session, List<ImageInfo> imgUrls,
            BiConsumer<MouseEvent, MudImgIcon> onDoubleClick) {
        uiThreadPool.execute(() -> {
            mudMain.printImg(session, imgUrls, onDoubleClick);
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
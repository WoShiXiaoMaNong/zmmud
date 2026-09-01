package zm.mud.ui;

import zm.mud.core.session.MudSession;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.ImageInfo;
import zm.mud.ui.component.MudImgIcon;
import zm.mud.ui.component.MudMainScreen;
import zm.mud.ui.component.MudTextArea;
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

class ImageDoubleClickListener implements BiConsumer<MouseEvent, MudImgIcon> {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ImageDoubleClickListener.class);

    @Override
    public void accept(MouseEvent e, MudImgIcon mudImgIcon) {
        // 在这里处理双击事件，例如打开图片预览窗口
        SwingUtilities.invokeLater(() -> {
            try {
                String url = mudImgIcon.getImgOriginUrl();
                JFrame previewFrame = new JFrame("Image Preview");
                previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                ImageIcon imageIcon = new ImageIcon(new java.net.URL(url));
                JLabel imageLabel = new JLabel(imageIcon);
                previewFrame.getContentPane().add(imageLabel);

                // 1. 先让组件根据图片大小进行初始布局计算
                previewFrame.pack();

                // 2. 如果 pack 后的窗口尺寸小于 200，则强制设为 200，否则保持原样
                int finalWidth = Math.max(200, previewFrame.getWidth());
                int finalHeight = Math.max(200, previewFrame.getHeight());

                // 3. 设置最终窗口大小并居中
                previewFrame.setSize(finalWidth, finalHeight);
                previewFrame.setLocationRelativeTo(null);
                previewFrame.setVisible(true);

            } catch (Exception ex) {
                logger.error("Error while previewing image: " + ex.getMessage(), ex);
            }
        });
    }

}

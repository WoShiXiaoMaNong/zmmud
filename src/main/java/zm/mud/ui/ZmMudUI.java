package zm.mud.ui;

import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;
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
        if (e.getClickCount() != 2) {
            return;
        }

        String url = mudImgIcon.getImgOriginUrl();
        if (url == null || url.isEmpty()) {
            return;
        }

       // --- 1. 立即响应：在 EDT 线程中立刻创建并弹出预览窗口 ---
        JFrame previewFrame = new JFrame("Image Preview");
        previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JLabel statusLabel = new JLabel("正在加载图片...", SwingConstants.CENTER);
        previewFrame.getContentPane().add(statusLabel, java.awt.BorderLayout.CENTER);
        
        previewFrame.setSize(200, 200);

        // 找到当前图片组件所属的顶层主 Frame
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(mudImgIcon);
        if (parentWindow != null) {
            // 如果找到了主界面，则相对于主界面居中
            previewFrame.setLocationRelativeTo(parentWindow);
        } else {
            // 兜底方案：如果找不到，再退回到屏幕居中
            previewFrame.setLocationRelativeTo(null);
        }
        
        previewFrame.setVisible(true);

        // --- 2. 异步下载：把耗时的网络请求丢到后台线程 ---
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                // 耗时的网络连接和图片解码发生在后台线程
                return new ImageIcon(new java.net.URL(url));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, ZmmudThreadPools.MUD_UI_IMG_DOWNLOAD.getExecutor()).thenAcceptAsync(imageIcon -> {
            // --- 3. 异步回调：下载成功，回到 EDT 线程更新 UI ---
            
            // 检查窗口是否已经被用户提前关闭，如果关闭了就不处理
            if (!previewFrame.isDisplayable()) {
                return;
            }

            // 移除加载提示，换成真正的图片标签
            previewFrame.getContentPane().remove(statusLabel);
            JLabel imageLabel = new JLabel(imageIcon);
            previewFrame.getContentPane().add(imageLabel, java.awt.BorderLayout.CENTER);

            // 重新计算布局
            previewFrame.pack();

            // 依然保持最小 200x200 的尺寸限制
            int finalWidth = Math.max(200, previewFrame.getWidth());
            int finalHeight = Math.max(200, previewFrame.getHeight());

            previewFrame.setSize(finalWidth, finalHeight);
            
            // 重新居中（可选：如果你希望它根据新大小重新在屏幕居中，可以解开下行注释）
            // previewFrame.setLocationRelativeTo(null);
            
            // 刷新界面绘制
            previewFrame.revalidate();
            previewFrame.repaint();

        }, SwingUtilities::invokeLater).exceptionally(ex -> {
            // --- 4. 异常处理：下载失败，回到 EDT 线程提示用户 ---
            SwingUtilities.invokeLater(() -> {
                if (previewFrame.isDisplayable()) {
                    statusLabel.setText("❌ 图片加载失败: Url [" + url + "] 错误: " + ex.getMessage());
                    statusLabel.setForeground(java.awt.Color.RED);
                }
            });
            logger.error("Error while previewing image from URL [" + url + "]: " + ex.getMessage(), ex);
            return null;
        });
    }



}

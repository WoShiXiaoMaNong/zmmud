package zm.mud.ui.component;

import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.ui.component.image.MudImgIcon;

public class MudPopup extends JFrame {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudPopup.class);

    private JLabel statusLabel;
    private MudSession session;

    public static void popupImg(MudSession session, MudImgIcon mudImgIcon) {
        String url = mudImgIcon.getImgOriginUrl();
        if (url == null || url.isEmpty()) {
            return;
        }

        // --- 1. 立即响应：在 EDT 线程中立刻创建并弹出预览窗口 ---
        MudPopup previewFrame = new MudPopup(session, "图片预览", "正在加载图片...");

        // 找到当前图片组件所属的顶层主 Frame
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(mudImgIcon);
        if (parentWindow != null) {
            // 如果找到了主界面，则相对于主界面居中
            previewFrame.setLocationRelativeTo(parentWindow);
        } else {
            // 兜底方案：如果找不到，再退回到屏幕居中
            previewFrame.setLocationRelativeTo(null);
        }

        previewFrame.showPopup(new Function<MudPopup, JComponent>() {
            @Override
            public javax.swing.JComponent apply(MudPopup popup) {
                try {
                    ImageIcon imageIcon = new ImageIcon(new java.net.URL(url));
                    return new JLabel(imageIcon);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "❌ 图片加载失败: Url [" + url + "] 错误");
    }

    public MudPopup(MudSession session, String title, String initialMsg) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.statusLabel = new JLabel(initialMsg, SwingConstants.CENTER);
        this.getContentPane().add(statusLabel, java.awt.BorderLayout.CENTER);
        this.setSize(200, 200);
        this.session = session;
    }

    public MudSession getSession() {
        return session;
    }

    public void showPopup(Function<MudPopup, JComponent> showPopupItem, String errorMsg) {
        this.setVisible(true);
        // --- 2. 异步下载：把耗时的网络请求丢到后台线程 ---
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                // 耗时的网络连接和图片解码发生在后台线程
                return showPopupItem.apply(this);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }, ZmmudThreadPools.MUD_UI_IMG_DOWNLOAD.getExecutor()).thenAcceptAsync(component -> {
            // --- 3. 异步回调：下载成功，回到 EDT 线程更新 UI ---

            // 检查窗口是否已经被用户提前关闭，如果关闭了就不处理
            if (!this.isDisplayable()) {
                return;
            }

            // 移除加载提示，换成真正的图片标签
            this.getContentPane().remove(statusLabel);
            this.getContentPane().add(component, java.awt.BorderLayout.CENTER);

            // 重新计算布局
            this.pack();

            // 依然保持最小 200x200 的尺寸限制
            int finalWidth = Math.max(200, this.getWidth());
            int finalHeight = Math.max(200, this.getHeight());

            this.setSize(finalWidth, finalHeight);

            // 刷新界面绘制
            this.revalidate();
            this.repaint();

        }, SwingUtilities::invokeLater).exceptionally(ex -> {
            // --- 4. 异常处理：下载失败，回到 EDT 线程提示用户 ---
            SwingUtilities.invokeLater(() -> {
                if (this.isDisplayable()) {
                    this.statusLabel.setText(errorMsg);
                    this.statusLabel.setForeground(java.awt.Color.RED);
                }
            });
            logger.error("Error while showing popup" + ex.getMessage(), ex);
            return null;
        });
    }
}

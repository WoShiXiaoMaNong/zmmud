package zm.mud.ui.component.image;

import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import zm.mud.core.thread.ZmmudThreadPools;

public class ImageDoubleClickListener implements BiConsumer<MouseEvent, MudImgIcon> {

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
        JFrame previewFrame = new JFrame("图片预览");
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

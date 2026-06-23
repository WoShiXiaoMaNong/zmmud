package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FullmeImgPopup extends JDialog {
    private static final Logger logger = LogManager.getLogger(FullmeImgPopup.class);

     private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                                    1, 1, 60L, TimeUnit.SECONDS,
                                    new LinkedBlockingQueue<>(1024),
                                    r -> {
                                        Thread t = new Thread(r,   "fullme-thread");
                                        t.setDaemon(true); // 强烈建议：客户端退出时，这些线程会自动销毁
                                        return t;
                                    },
                                    new ThreadPoolExecutor.CallerRunsPolicy()
                            );
    
    private final JLabel imageLabel;
    private final JScrollPane scrollPane;
    private final JButton refreshButton; // 刷新按钮
    
    private String currentImgUrl; // 记录当前的图片 URL，供刷新时使用

    public FullmeImgPopup() {
        super((Frame) null, "Fullme", true);
        this.setSize(300, 350); // 稍微调高一点高度（原本300），给底部的按钮留出空间
        this.setResizable(true); 
        this.setLocationRelativeTo(null); 
        
        // 1. 初始化图片承载结构
        this.imageLabel = new JLabel();
        this.imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        this.scrollPane = new JScrollPane(imageLabel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // 2. 创建底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.refreshButton = new JButton("刷新图片");
        buttonPanel.add(refreshButton);

        // 3. 为刷新按钮绑定点击事件
        this.refreshButton.addActionListener(e -> {
            if (this.currentImgUrl != null) {
                // 关键改进：确保这几行代码在 Swing 线程中拥有最高优先级的渲染权
                this.refreshButton.setEnabled(false); 
                this.refreshButton.setText("加载中...");
                
                // 强制让按钮立刻刷新其外观
                this.refreshButton.paintImmediately(this.refreshButton.getBounds());

                // 按钮变色/变字完成后，再启动子线程去下载
                loadImageAsync(this.currentImgUrl);
            }
        });


        // 4. 组装整体布局
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH); // 把按钮栏放在底部
        this.setVisible(false);
    }

    /**
     * 外部调用的唯一入口
     */
    public void show(String imgUrl) {
        this.currentImgUrl = imgUrl; // 备份 URL 供刷新使用
        
        // 重置按钮状态
        this.refreshButton.setEnabled(true);
        this.refreshButton.setText("刷新图片");
        
        // 异步加载图片
        loadImageAsync(imgUrl);
    }

    /**
     * 核心的异步加载图片逻辑
     */
    private void loadImageAsync(String imgUrl) {
        threadPool.execute(() -> {
            java.net.HttpURLConnection connection = null;
            java.io.InputStream inputStream = null;
            try {
                logger.info("开始下载fullme图片: " + imgUrl);
                
                // 1. 严格使用原始 URL，不追加任何自定义参数防止服务器404
                URL url = new URL(imgUrl);
                connection = (java.net.HttpURLConnection) url.openConnection();
                
                // 2. 关键点：伪装成标准的桌面浏览器 User-Agent，绕过服务器防火墙拦截
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                // 3. 显式禁用客户端缓存，达到与时间戳相同的刷新效果
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Pragma", "no-cache");
                
                connection.setConnectTimeout(5000); // 设置连接超时 5 秒
                connection.setReadTimeout(5000);    // 设置读取超时 5 秒

                // 获取响应状态码
                int responseCode = connection.getResponseCode();
                if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                    throw new java.io.IOException("服务器返回错误状态码: " + responseCode);
                }

                // 4. 从连接中获取输入流并交给 ImageIO 解析
                inputStream = connection.getInputStream();
                BufferedImage image = ImageIO.read(inputStream);

                if (image == null) {
                    logger.error("Load image error! URL returned no image data.");
                    resetButtonOnMainThread();
                    return;
                }

                // 下载成功，切回 UI 线程更新界面
                SwingUtilities.invokeLater(() -> {
                    this.imageLabel.setIcon(new ImageIcon(image));
                    
                    this.refreshButton.setEnabled(true);
                    this.refreshButton.setText("刷新图片");
                    
                    this.revalidate();
                    this.repaint();
                    
                    if (!this.isVisible()) {
                        this.setVisible(true);
                    }
                });

            } catch (Exception e) {
                logger.error("Load fullme img error! " + imgUrl, e);
                resetButtonOnMainThread();
            } finally {
                // 5. 优雅关闭流和连接，释放网络资源
                if (inputStream != null) {
                    try { inputStream.close(); } catch (Exception ignored) {}
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * 加载失败时，安全的在主线程恢复按钮状态
     */
    private void resetButtonOnMainThread() {
        SwingUtilities.invokeLater(() -> {
            this.refreshButton.setEnabled(true);
            this.refreshButton.setText("刷新失败(重试)");
        });
    }
}

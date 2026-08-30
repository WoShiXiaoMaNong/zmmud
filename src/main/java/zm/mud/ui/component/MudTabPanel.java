package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.session.MudSession;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.statusBar.MudStatusBar;

public class MudTabPanel {
    private static final Logger logger = LogManager.getLogger(MudTabPanel.class);

  
    private MudTextArea textArea;
    //private JScrollPane tabPanelArea;
    private MudSession session;

    private JPanel tabMainPanel;

    private MudStatusBar mudStatusBar;

    private MudInputField mudInputField;

    private JLabel lblTitle;

    public MudTabPanel(MudSession session, GlobalCfg cfg, Dimension dimension) {
        this.session = session;
        this.tabMainPanel = new JPanel();
        this.tabMainPanel.setPreferredSize(dimension);
       
        this.init(cfg);
    }

    private void init(GlobalCfg cfg) {
        this.tabMainPanel.setLayout(new BorderLayout());

        // ================== 【状态栏放在最上方】 ==================
        this.mudStatusBar = new MudStatusBar();
        this.tabMainPanel.add(this.mudStatusBar, BorderLayout.NORTH);
        // =============================================================

        // 设置文本区固定高度
        this.textArea = new MudTextArea(cfg);
        Dimension maxDimension = this.tabMainPanel.getPreferredSize();
        this.textArea.setPreferredSize(new Dimension(maxDimension.width, maxDimension.height - 30));
        JScrollPane scrollPane = new MudScrollPane(this.textArea, (isBottom) -> {
            textArea.setAutoScrollEnabled(isBottom); // 设置 MudTextAare 的自动滚动状态
            return textArea;
        });
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(textArea.getPreferredSize()); // 高度固定150px

        this.tabMainPanel.add(scrollPane, BorderLayout.CENTER);

        // 输入框在底部
        this.mudInputField = new MudInputField(this.session,this);
        this.tabMainPanel.add(this.mudInputField, BorderLayout.SOUTH);

    }


    public void addMe(JTabbedPane tabBar, MudMainScreen mainScreen) {
        // 将整个 UI 变更放入 Swing 的事件派发线程，防止由于多线程导致界面不重绘或死锁
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // 防呆检查：确保组件已实例化
                if (this.tabMainPanel == null) {
                    logger.error("【错误】this.tabPanelArea 为 null，无法添加！");
                    return;
                }

                int tabCount = tabBar.getTabCount();

                // 防越界逻辑
                // 如果当前没有任何 Tab（count=0），索引必须为 0；
                // 如果已有 Tab（最后一个是"+"号），才插入到倒数第一位（count - 1）
                int insertIndex = (tabCount <= 0) ? 0 : (tabCount - 1);

                // 确保该组件如果之前在别的容器里，先解除绑定，防止 Swing 容器冲突
                if (this.tabMainPanel.getParent() != null) {
                    this.tabMainPanel.getParent().remove(this.tabMainPanel);
                }

                // 1. 插入实际内容组件
                tabBar.insertTab(this.session.getSessionName(), null, this.tabMainPanel, "切换至 " + this.session.getSessionName(), insertIndex);

                // 2. 绑定自定义的 Tab 标签头部
                javax.swing.JPanel tabHeader = createTabHeader(this.session.getSessionName(), this.tabMainPanel, tabBar, mainScreen);
                tabBar.setTabComponentAt(insertIndex, tabHeader);

                // 3. 选中当前新创建的 Tab
                tabBar.setSelectedIndex(insertIndex);

            } catch (Exception e) {
                logger.error("Init Tab error!",e);
            }
        });
    }

    public String getTabName() {
        return this.session.getSessionName();
    }

    /**
     * 辅助方法：构建一个包含“标题文本”和“X按钮”的标签头组件
     * 
     * @param title            标签页标题
     * @param contentComponent 该标签对应的实际内容组件（用于点击关闭时定位并移除）
     */
    private JPanel createTabHeader(String title, final JComponent contentComponent,
            JTabbedPane tabBar, MudMainScreen mainScreen) {
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlHeader.setOpaque(false); // 设置透明，使其与 Tab 的主题背景融合

        // 标题文本
        this.lblTitle = new JLabel(title);
        pnlHeader.add(lblTitle);

        // 关闭按钮 "x"
        JButton btnClose = new JButton("x");
        btnClose.setFont(new Font("Arial", Font.BOLD, 11));
        btnClose.setMargin(new java.awt.Insets(0, 4, 0, 4)); // 让按钮小巧一点
        btnClose.setContentAreaFilled(false); // 移除默认的按钮背景色
        btnClose.setBorderPainted(false); // 移除边框线
        btnClose.setFocusable(false);

        // 鼠标悬停变红的视觉小优化
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(java.awt.Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(Color.BLACK);
            }
        });

        // 点击 X 时的关闭逻辑
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. 弹出二次确认框
                int result = JOptionPane.showConfirmDialog(
                        mainScreen,
                        "确定要关闭该会话标签吗？\n关闭后，当前会话对应的网络连接将被断开。",
                        "提示",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                // 如果用户没有点“是”，直接拦截不执行后续逻辑
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }

                // 2. 用户确认后，通过内容组件动态查找当前最新的索引并删除
                int index = tabBar.indexOfComponent(contentComponent);
                if (index != -1) {
                    tabBar.removeTabAt(index);

                    // 优雅降级选中
                    int currentSelected = tabBar.getSelectedIndex();
                    int tabCount = tabBar.getTabCount();

                    if (tabCount > 1) {
                        if (currentSelected >= tabCount - 1) {
                            tabBar.setSelectedIndex(tabCount - 2);
                        }
                    } else {
                        tabBar.setSelectedIndex(0);
                    }
                }
            }
        });

        pnlHeader.add(btnClose);
        return pnlHeader;
    }

    public void printlnToScreen(String msg) {
        this.printlnToScreen(msg,false);
    }

    public void printlnToScreen(String msg,boolean enabled) {
        this.textArea.printlnToScreen(msg,enabled);
    }

    public int getMsgOffSet(String msg) {
        return this.textArea.getMsgOffset(msg);
    }

    public void printImg(List<ImageInfo> imgUrls, int offset) {
        this.textArea.printImg(imgUrls,offset);
    }

    public void printImg(List<ImageInfo> imgUrls) {
        this.textArea.printImg(imgUrls);
    }

    public void refreshStatusBar(Map<String, Object> statusData, PkuxkxRoom room) {
        this.mudStatusBar.refreshStatus(statusData, room);
    }

    public void resetFont(String font, int size) {
        this.textArea.setFont(new Font(font, Font.PLAIN, size));
    }

    public void setTitle(String title) {
        String currentTitle = this.lblTitle.getText();
        this.lblTitle.setText(currentTitle + title);
    }

}

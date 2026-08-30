package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import zm.mud.core.api.ClientService;
import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.utils.SpringBeanUtil;

public class MudMainScreen extends JFrame {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudMainScreen.class);

    private GlobalCfg globleCfg;

    private ZmMudUI ui;

    private JTabbedPane tabbedPane;

    private volatile String selectedSession;

    private volatile Map<String/* Session ID */, MudTabPanel> tabPanels;

    public MudMainScreen(GlobalCfg cfg, ZmMudUI ui) {
        this.globleCfg = cfg;
        this.ui = ui;
        setTitle(this.globleCfg.getTitle());
        setSize(new Dimension(this.globleCfg.getWidth(), this.globleCfg.getHeight()));

        tabPanels = new ConcurrentHashMap<>();

        // 1. 改成 DO_NOTHING，把红叉的控制权拿回手里
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 2. 绑定窗口关闭事件监听器
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 在后台或当前线程中优雅发送 quit，并层层关闭本地网络句柄
                logger.info("用户点击关闭窗口，开始执行安全断开流程...");
                try {
                    SpringBeanUtil.getBean(ClientService.class).quit();
                } catch (Exception ex) {
                    logger.error("安全断开期间发生异常: ", ex);
                } finally {
                    logger.info("网络资源已安全释放，进程即将安全退出。");
                    System.exit(0);
                }
            }
        });
        setLocationRelativeTo(null);
        this.init();
        pack(); // 根据组件首选大小调整窗口
        setLocationRelativeTo(null);

        ZmmudThreadPools.MUD_UI.execute(() -> {
            logger.info("Status bar refresh loop start");
            while (true) {
                for (Entry<String, MudTabPanel> entry : tabPanels.entrySet()) {
                    MudTabPanel mTabPanel = entry.getValue();
                    String sessionId = entry.getKey();
                    if (selectedSession == null || !selectedSession.equals(sessionId)) {
                        continue;
                    }
                    MudSession session = MudSession.getSession(sessionId);
                    GMCPContext gmcpContext = session.getGmcpContext();
                    mTabPanel.refreshStatusBar(gmcpContext.getStatus(), gmcpContext.getCurrentRoom());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        logger.error("Error while refreshing status bar: ", e);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Error while refreshing status bar: ", e);
                }

            }
        });
    }

    private void createNewSession(String title) {
        MudSession session = MudSession.newSession();
        session.setSessionName(title);
        addNewTab(session);
        session.start();
        this.selectedSession = session.getSessionId();
        // 刷新容器布局和重绘，保证新布局立刻生效
        this.revalidate();
        this.repaint();
        SwingUtilities.invokeLater(() -> {
            this.foucesInputLine();
        });

    }

    private void init() {
        this.setLayout(new BorderLayout());

        // 1. 最上面：预留的 menu bar 留空

        // 2. 中间：Tab 管理组件（贴在顶部一行）
        tabbedPane = new javax.swing.JTabbedPane(javax.swing.JTabbedPane.TOP) {
            @Override
            public void updateUI() {
                super.updateUI();
                // 针对某些 LookAndFeel 的底层 UI 覆写
                if (getUI() instanceof javax.swing.plaf.basic.BasicTabbedPaneUI) {
                    setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                        @Override
                        protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
                            // 留空：绝对不绘制任何下方和四周的内容边框线
                        }
                    });
                }
            }
        };

        // 【核心修复】彻底消除 TabbedPane 的外边框和不必要的空白断层
        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        // 如果使用的LookAndFeel支持，还可以进一步强制其内部页边距为0
        javax.swing.UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
        javax.swing.UIManager.put("TabbedPane.tabAreaInsets", new java.awt.Insets(0, 0, 0, 0));

        // 初始化时，先添加一个空的占位 Tab 用于承载 "+" 号组件
        tabbedPane.addTab("", new javax.swing.JPanel());
        // 为 "+" 号设置专用的自定义点击组件
        tabbedPane.setTabComponentAt(0, createAddTabButton());

        // 放入 NORTH
        int tabBarHeight = 20; // 紧凑的 TabBar 高度
        int availableHeight = Math.max(0, this.globleCfg.getHeight() - tabBarHeight);
        Dimension dimension = new Dimension(this.globleCfg.getWidth(), availableHeight);
        tabbedPane.setPreferredSize(new Dimension(dimension));
        tabbedPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        this.add(tabbedPane, BorderLayout.CENTER);

        // ==========================================
        // 3. 最下方：初始化时的空占位面板（撑开主界面）
        // ==========================================
        // tabMainPanel = new javax.swing.JPanel();

        // // 精准计算高度
        // int tabBarHeight = 20; // 紧凑的 TabBar 高度
        // int availableHeight = Math.max(0, this.globleCfg.getHeight() - tabBarHeight);

        // tabMainPanel.setPreferredSize(new Dimension(this.globleCfg.getWidth(),
        // availableHeight));
        // tabMainPanel.setBackground(java.awt.Color.LIGHT_GRAY); // 你的大面积灰色背景

        // // 消除面板自身可能带有微小间距的隐患
        // tabMainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        // 放入 CENTER，由于移除了所有 Border，它会与 NORTH 的 Tab 栏像素级无缝拼接
        // this.add(tabMainPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建专用于新增 Tab 的 "+" 号按钮组件
     */
    private javax.swing.JComponent createAddTabButton() {
        javax.swing.JLabel lblAdd = new javax.swing.JLabel("  +  ");
        lblAdd.setFont(new Font("Arial", Font.BOLD, 14));
        lblAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // 鼠标悬停变色提示
        lblAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblAdd.setForeground(java.awt.Color.BLUE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblAdd.setForeground(java.awt.Color.BLACK);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 点击 "+" 号直接弹出输入框并创建新 Tab
                String tabName = javax.swing.JOptionPane.showInputDialog(
                        MudMainScreen.this,
                        "请输入新会话名称:",
                        "新建 Tab",
                        javax.swing.JOptionPane.PLAIN_MESSAGE);

                if (tabName != null && !tabName.trim().isEmpty()) {
                    createNewSession(tabName.trim());
                }
            }
        });

        return lblAdd;
    }

    /**
     * 辅助方法：始终把新 Tab 插入到最后那个 "+" 号组件的左侧
     */
    private MudTabPanel addNewTab(MudSession session) {
        // 1. 此时直接获取 tabMainPanel 预留的完整空间尺寸

        // 2. 实例化你的 MudTabPanel
        MudTabPanel newTab = new MudTabPanel(session, this.globleCfg, this.tabbedPane.getPreferredSize());
        this.tabPanels.put(session.getSessionId(), newTab);

  
        newTab.resetFont(this.globleCfg.getFontName(), this.globleCfg.getFontSize());

        // 3. 调用 addMe，它会将黑色的 tabPanelArea 完美嵌入顶部的 tabbedPane 中
        newTab.addMe(this.tabbedPane, this);

              this.tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 获取当前选中的组件或索引
                Component selectedComponent = tabbedPane.getSelectedComponent();
                if (selectedComponent != null) {
                    selectedSession = selectedComponent.getName();
                    foucesInputLine();
                }
            }
        });

        // 4. 清理：因为组件现在都在 tabbedPane 内部了，下面这行要删掉，防止两头重复添加冲突
        // tabMainPanel.add(newTab.getTextArea()); <-- 删掉这行

        return newTab;
    }

    public void foucesInputLine() {
        if(this.selectedSession == null){
            return;
        }
        MudTabPanel selectedTab = this.tabPanels.get(this.selectedSession);
        if (selectedTab == null) {
            return;
        }
        selectedTab.focusInputLine();
    }

    public void setShow() {
        this.setVisible(true);
    }

    public void resetFont(MudSession session, String font, int size) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.resetFont(font, size);
    }

    public void refreshStatusBar(MudSession session, Map<String, Object> statusData, PkuxkxRoom room) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.refreshStatusBar(statusData, room);
    }

    public void printlnToScreen(MudSession session, String msg, boolean enableBlod) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.printlnToScreen(msg, enableBlod);
    }

    public void printlnToScreen(MudSession session, String msg) {
        this.printlnToScreen(session, msg, false);
    }

    public int getMsgOffset(MudSession session, String msg) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        return mudTabPanel.getMsgOffSet(msg);
    }

    /**
     * @see MudTextArea#printImg(String, int)
     * @param imgUrl
     * @param offset
     */
    public void printImg(MudSession session, List<ImageInfo> imgUrls, int offset) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.printImg(imgUrls, offset);
    }

    public void printImg(MudSession session, List<ImageInfo> imgUrls) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.printImg(imgUrls);
    }

    public void setTitle(MudSession session, String title) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.setTitle(title);
    }

}

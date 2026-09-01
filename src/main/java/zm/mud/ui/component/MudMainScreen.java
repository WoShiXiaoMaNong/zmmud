package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import zm.mud.core.api.ClientService;
import zm.mud.core.api.InbMsgService;
import zm.mud.core.session.MudSession;
import zm.mud.core.thread.ZmmudThreadPools;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.image.ImageInfo;
import zm.mud.ui.component.image.MudImgIcon;
import zm.mud.ui.processor.MsgPrintProcessor;
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
                if (tabPanels == null || tabPanels.isEmpty()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error("Error while refreshing status bar: ", e);
                    }
                }
                for (Entry<String, MudTabPanel> entry : tabPanels.entrySet()) {
                    MudTabPanel mTabPanel = entry.getValue();
                    String sessionId = entry.getKey();
                    if (selectedSession == null || !selectedSession.equals(sessionId)) {
                        continue;
                    }
                    MudSession session = MudSession.getSession(sessionId);
                    GMCPContext gmcpContext = session.getGmcpContext();
                    mTabPanel.refreshStatusBar(gmcpContext.getGmcpData());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.error("Error while refreshing status bar: ", e);
                    }
                }

            }
        });
    }

    private void createNewSession(String title,String host,int port) {
        MudSession session = MudSession.newSession(host,port);
        session.setSessionName(title);
        this.addNewTab(session);
        session.start();
        this.selectedSession = session.getSessionId();
        // 刷新容器布局和重绘，保证新布局立刻生效
        this.revalidate();
        this.repaint();
        SwingUtilities.invokeLater(() -> {
            this.foucesInputLine();
        });
        
   
        MsgPrintProcessor msgPinter = SpringBeanUtil.getBean(MsgPrintProcessor.class);
  
        InbMsgService inbMsgService = SpringBeanUtil.getBean(InbMsgService.class);

        inbMsgService.registerMsgHandler(session,msgPinter);
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
                            // 留空：不绘制任何下方和四周的内容边框线
                        }
                    });
                }
            }
        };

        this.tabbedPane.addChangeListener(new TabbedPanelChangeListener());


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
                showConnectDialog();
            }
        });

        return lblAdd;
    }


/**
 * 弹出连接MUD世界的对话框
 */
private void showConnectDialog() {
    // 1. 创建包含三个输入框的面板
    javax.swing.JPanel inputPanel = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 5, 5));

    javax.swing.JTextField nameField = new javax.swing.JTextField();
    javax.swing.JTextField hostField = new javax.swing.JTextField();
    javax.swing.JTextField portField = new javax.swing.JTextField();

    inputPanel.add(new javax.swing.JLabel("MUD世界名称:"));
    nameField.setText(globleCfg.getDefaultServerName());
    inputPanel.add(nameField);
    inputPanel.add(new javax.swing.JLabel("Host (主机):"));
    inputPanel.add(hostField);
    hostField.setText(globleCfg.getDefaultHost());
    inputPanel.add(new javax.swing.JLabel("Port (端口):"));
    portField.setText(String.valueOf(globleCfg.getDefaultPost()));
    inputPanel.add(portField);

    // 2. 创建主面板，把输入面板和自定义的按钮放进去
    javax.swing.JPanel mainPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
    mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.add(inputPanel, java.awt.BorderLayout.CENTER);

    // 3. 创建底部的 确定/取消 按钮
    javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
    javax.swing.JButton okButton = new javax.swing.JButton("确定");
    javax.swing.JButton cancelButton = new javax.swing.JButton("取消");
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

    // 4. 使用 JDialog 承载这个面板
    final javax.swing.JDialog dialog = new javax.swing.JDialog(MudMainScreen.this, "连接MUD世界", true); // true 表示模态窗口
    dialog.setContentPane(mainPanel);

    // 焦点在确定按钮上时，按回车就能直接触发点击了
    dialog.getRootPane().setDefaultButton(okButton);

    // 5. 绑定“确定”按钮的点击事件（核心逻辑）
    okButton.addActionListener(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            String tabName = nameField.getText().trim();
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();

            // 基础非空校验
            if (!tabName.isEmpty() && !host.isEmpty() && !portStr.isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr);
                    // 校验成功：创建新会话并关闭弹框
                    createNewSession(tabName, host, port);
                    dialog.dispose(); 
                } catch (NumberFormatException ex) {
                    // 端口错误提示：挂载在当前 dialog 之上，不会导致 dialog 消失
                    javax.swing.JOptionPane.showMessageDialog(
                            dialog,
                            "端口号必须为数字！",
                            "输入错误",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // 空字段提示
                javax.swing.JOptionPane.showMessageDialog(
                        dialog,
                        "所有字段均不能为空！",
                        "输入错误",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    });

    // 6. 绑定“取消”按钮事件
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            dialog.dispose();
        }
    });

    // 7. 渲染并显示弹框
    dialog.pack();
    dialog.setLocationRelativeTo(MudMainScreen.this); // 居中显示在主窗口
    // 异步请求焦点，确保在窗口渲染完毕后“确定”按钮拿到焦点
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            okButton.requestFocusInWindow();
        }
    });
    dialog.setVisible(true);
}


    /**
     * 辅助方法：始终把新 Tab 插入到最后那个 "+" 号组件的左侧
     */
    private MudTabPanel addNewTab(MudSession session) {
        // 1. 此时直接获取 tabMainPanel 预留的完整空间尺寸

        // 2. 实例化你的 MudTabPanel
        MudTabPanel newTab = new MudTabPanel(session, this.globleCfg, this.tabbedPane.getPreferredSize());
        this.tabPanels.put(session.getSessionId(), newTab);
        newTab.applyTheme(this.globleCfg.getThemeType().getTheme());
        newTab.resetFont(this.globleCfg.getFontName(), this.globleCfg.getFontSize());

        // 3. 调用 addMe，它会将黑色的 tabPanelArea 完美嵌入顶部的 tabbedPane 中
        newTab.addMe(this.tabbedPane, this);

        return newTab;
    }

    public void foucesInputLine() {
        if (this.selectedSession == null) {
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
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showConnectDialog();
            }
        });
    }

    public void resetFont(MudSession session, String font, int size) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.resetFont(font, size);
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
    public void printImg(MudSession session, List<ImageInfo> imgUrls, int offset,BiConsumer<MouseEvent,MudImgIcon> onDoubleClick) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.printImg(imgUrls, offset, onDoubleClick);
    }

    public void printImg(MudSession session, List<ImageInfo> imgUrls,BiConsumer<MouseEvent,MudImgIcon> onDoubleClick) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.printImg(imgUrls, onDoubleClick);
    }

    public void setTitle(MudSession session, String title) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.setTitle(title);
    }


    private class TabbedPanelChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            // 1. 保留你原有的业务逻辑
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent != null) {
                selectedSession = selectedComponent.getName();
                foucesInputLine();
            }

            // 2. 刷新所有标签的视觉状态
            int selectedIndex = tabbedPane.getSelectedIndex();
            int tabCount = tabbedPane.getTabCount();

            // 主题色配置
            Color selectedColor = new Color(0, 102, 51);
            Color unselectedColor = Color.BLACK; // 未选中时暗灰

            for (int i = 0; i < tabCount; i++) {
                Component header = tabbedPane.getTabComponentAt(i);

                if (header instanceof JPanel) {
                    JPanel pnlHeader = (JPanel) header;
                    
                    // 【新增：边框控制】根据是否选中，为面板设置虚线边框或清空边框
                    if (i == selectedIndex) {
                        // 参数依次为：颜色、线宽、虚线长度、间距、是否圆角
                        pnlHeader.setBorder(BorderFactory.createDashedBorder(selectedColor, 1.0f, 2.0f, 2.0f, false));
                    } else {
                        pnlHeader.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // 清空边框，保持 1 像素占位防止界面抖动
                    }

                    for (Component child : pnlHeader.getComponents()) {
                        if (child instanceof JLabel) {
                            // 关键点：强转为 JLabel 对象
                            JLabel lblTitle = (JLabel) child;

                            if (i == selectedIndex) {
                                // 【选中】设置字体加粗，单独调用 setForeground 设置颜色
                                lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
                                lblTitle.setForeground(selectedColor);
                            } else {
                                // 【未选中】设置字体常规，单独调用 setForeground 设置颜色
                                lblTitle.setFont(lblTitle.getFont().deriveFont(Font.PLAIN));
                                lblTitle.setForeground(unselectedColor);
                            }
                            break; // 找到了标签文本，跳出当前 Panel 的循环
                        }
                    }
                    
                    // 确保边框修改后立即重绘
                    pnlHeader.revalidate();
                    pnlHeader.repaint();
                }
            }
        }

    }


    public void setCurrentUserName(MudSession session, String userName) {
        String sessionId = session.getSessionId();
        MudTabPanel mudTabPanel = this.tabPanels.get(sessionId);
        mudTabPanel.setUserName(userName);
    }



}

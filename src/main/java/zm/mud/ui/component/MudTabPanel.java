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
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.session.MudSession;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.image.ImageInfo;
import zm.mud.ui.component.image.MudImgIcon;
import zm.mud.ui.component.statusBar.MudStatusBar;
import zm.mud.ui.theme.ITheme;

public class MudTabPanel implements IMudUiComponent{
    private static final Logger logger = LogManager.getLogger(MudTabPanel.class);

  
    private MudTextArea textArea;
  
    private MudSession session;

    private JPanel tabMainPanel;

    private MudStatusBar mudStatusBar;

    private MudInputField mudInputField;

    private JLabel lblTitle;
    private String initTitle;

    public MudTabPanel(MudSession session, GlobalCfg cfg, Dimension dimension) {
        this.session = session;
        this.tabMainPanel = new JPanel();
        tabMainPanel.setName(session.getSessionId());
        this.tabMainPanel.setPreferredSize(dimension);
       
        this.init(cfg);
    }

    private void init(GlobalCfg cfg) {
        this.tabMainPanel.setLayout(new BorderLayout());

        // ================== 【状态栏放在最上方】 ==================
        this.mudStatusBar = new MudStatusBar(session);
        this.tabMainPanel.add(this.mudStatusBar, BorderLayout.NORTH);
        // =============================================================

        // 设置文本区固定高度
        this.textArea = new MudTextArea(this.session,cfg);
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

    public void focusInputLine(){
        this.mudInputField.requestFocusInWindow();
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
        // 增加 FlowLayout 的左右间距，并为整个头组件加上外边距，防止粘连
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlHeader.setOpaque(false); 
        // 给 Header 增加内边距（上, 左, 下, 右），撑开标签的整体高度和宽度
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));

        // 标题文本
        this.lblTitle = new JLabel(title);
        this.initTitle = title;
        // 可以微调字体，让它在未选中时也清晰
        this.lblTitle.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        pnlHeader.add(lblTitle);

        // 【重构关闭按钮 "x"
        // 1. 放弃纯文本 "x"，使用乘号 "×"（Unicode \u00D7），它在视觉上更丰满、更像关闭图标
        JButton btnClose = new JButton("\u00D7"); 
        btnClose.setFont(new Font("Arial", Font.BOLD, 18)); // 调大字号增强辨识度
        btnClose.setMargin(new java.awt.Insets(0, 4, 0, 4)); 
        btnClose.setContentAreaFilled(false); 
        btnClose.setBorderPainted(false); 
        btnClose.setFocusable(false);

        // 默认就开启边框绘制，但设置一个完全透明的空边框占位，防止尺寸变化
        btnClose.setBorderPainted(true);
        final Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        final Border hoverBorder = BorderFactory.createLineBorder(new Color(200, 80, 80, 100), 1);
        btnClose.setBorder(emptyBorder);
        
        // 显式固定按钮的最高/最新大小，防止 Unicode 字符在不同生命周期被计算出不同尺寸
        btnClose.setPreferredSize(new java.awt.Dimension(20, 20)); 
        
        // 2. 设置一个显眼的默认颜色（例如浅灰色，暗黑主题下更可见）
        final Color defaultBtnColor = Color.BLACK;
        btnClose.setForeground(defaultBtnColor);

        // 鼠标悬停交互升级：变红的同时增加虚线/浅色边框，提示可点击性
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(Color.RED);
                // 悬停时显示一个小边框，增强“显眼”度
                btnClose.setBorderPainted(true);
                 btnClose.setBorder(hoverBorder);
                //btnClose.setBorder(BorderFactory.createLineBorder(new Color(200, 80, 80, 100), 1));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(defaultBtnColor);
                 btnClose.setBorder(emptyBorder);
                //btnClose.setBorderPainted(false); // 移出时隐藏边框
            }
        });

        // 点击 X 时的关闭逻辑 (保持你原有的优秀业务逻辑不变)
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        mainScreen,
                        "确定要关闭该会话标签吗？\n关闭后，当前会话对应的网络连接将被断开。",
                        "提示",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }

                int index = tabBar.indexOfComponent(contentComponent);
                if (index != -1) {
                    tabBar.removeTabAt(index);
                
                    int currentSelected = tabBar.getSelectedIndex();
                    String selectedSessionId = contentComponent.getName();
                    MudSession session = MudSession.getSession(selectedSessionId);
                    if(session != null){
                        session.close();
                    }
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

    public void printImg(List<ImageInfo> imgUrls, int offset,BiConsumer<MouseEvent,MudImgIcon> onClick) {
        this.textArea.printImg(imgUrls,offset,onClick);
    }

    public void printImg(List<ImageInfo> imgUrls,BiConsumer<MouseEvent,MudImgIcon> onClick) {
        this.textArea.printImg(imgUrls,onClick);
    }

    public void refreshStatusBar( Map<String/* Channel Name */,Map<String,Object>> gmcpData) {
        this.mudStatusBar.refreshStatus(gmcpData);
    }

    public void resetFont(String font, int size) {
        this.textArea.setFont(new Font(font, Font.PLAIN, size));
    }

    public void setTitle(String title) {
        this.lblTitle.setText( this.initTitle + title);
    }

    @Override
    public void applyTheme(ITheme theme) {
        this.mudInputField.applyTheme(theme);
        this.mudStatusBar.applyTheme(theme);
    }

	public void setUserName(String userName) {
		this.mudInputField.setName(userName);
       
	}
    

}

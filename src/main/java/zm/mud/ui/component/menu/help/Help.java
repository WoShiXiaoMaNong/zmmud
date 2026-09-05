package zm.mud.ui.component.menu.help;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zm.mud.ui.component.menu.AbsZmMudDialog;

public class Help extends AbsZmMudDialog {
    private static final Logger logger = LogManager.getLogger(Help.class);
    
    private JList<String> menuList;
    private CardLayout cardLayout;
    private JPanel centerContentPanel;

    public Help(Frame owner, String title) {
        super(owner, title);
        // 设置弹窗的默认大小与居中属性
        this.setSize(650, 450);
        this.setLocationRelativeTo(owner);
    }

    @Override
    protected JPanel getContentPanelUi() {
        // 主面板采用 BorderLayout
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. 左侧导航菜单
        String[] menuItems = {"关于项目", "作者支持", "功能进度", "运行说明"};
        menuList = new JList<>(menuItems);
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setSelectedIndex(0);
        menuList.setFixedCellWidth(120);
        menuList.setFixedCellHeight(35);
        // 美化列表边框
        menuList.setBorder(BorderFactory.createEtchedBorder());

        // 2. 右侧卡片式内容面板
        cardLayout = new CardLayout();
        centerContentPanel = new JPanel(cardLayout);
        centerContentPanel.setBorder(BorderFactory.createEtchedBorder());

        // 添加各个子帮助页面
        centerContentPanel.add(createAboutPanel(), "关于项目");
        centerContentPanel.add(createAuthorPanel(), "作者支持");
        centerContentPanel.add(createProgressPanel(), "功能进度");
        centerContentPanel.add(createRunPanel(), "运行说明");

        // 3. 绑定菜单切换事件
        menuList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = menuList.getSelectedValue();
                cardLayout.show(centerContentPanel, selectedValue);
                logger.debug("切换帮助菜单至: {}", selectedValue);
            }
        });

        // 4. 组装主面板
        mainContentPanel.add(new JScrollPane(menuList), BorderLayout.WEST);
        mainContentPanel.add(centerContentPanel, BorderLayout.CENTER);

        return mainContentPanel;
    }

    /**
     * 创建"关于项目"面板
     */
    private JComponent createAboutPanel() {
        String html = "<html>" +
                "<h2 style='color:#2B2B2B;'>ZmMud - MUD Client</h2>" +
                "<p><b>ZmMud</b> 是一个基于 Java 实现的 MUD 游戏客户端项目。</p>" +
                "<p>目前正针对经典的 <b><a href='https://www.pkuxkx.net/#/'>北大侠客行</a></b> 进行适配与深度开发，当前处于早期开发阶段。</p>" +
                "<br>" +
                "<h3>🎯 项目目标：</h3>" +
                "<ul>" +
                "  <li>构建一个结构清晰、可扩展的 MUD 客户端</li>" +
                "  <li>支持 Telnet 协议（包含 IAC 控制指令处理）</li>" +
                "  <li>实现完整的消息收发、解析、处理流程</li>" +
                "  <li>后续功能：触发器、自动化</li>" +
                "</ul>" +
                "</html>";
        return createHtmlScrollPane(html);
    }

    /**
     * 创建"作者支持"面板
     */
    private JComponent createAuthorPanel() {
        String html = "<html>" +
                "<h2>✍️ 作者与支持</h2>" +
                "<hr>" +
                "<p>如果您在体验或开发过程中遇到任何 Bug，或者有更好的功能建议，欢迎通过邮件联系作者。</p>" +
                "<br>" +
                "<p><b>电子邮箱：</b> <a href='mailto:zhongming139@126.com'>zhongming139@126.com</a></p>" +
                "<br><br>" +
                "<p style='color:gray;'><i>* 提示：更多关于北大侠客行及触发器的详细说明，可参阅项目根目录下的 Pkuxkx.md 与 Trigger.md 文档。</i></p>" +
                "</html>";
        return createHtmlScrollPane(html);
    }

    /**
     * 创建"功能进度"面板
     */
    private JComponent createProgressPanel() {
        String html = "<html>" +
                "<h2>⚙️ 当前开发进度</h2>" +
                "<table border='0' cellpadding='3'>" +
                "  <tr><td>✅ <b>多标签支持</b></td><td>(多 Session 同时多开)</td></tr>" +
                "  <tr><td>✅ <b>网络连接能力</b></td><td>(入站/出站分层)</td></tr>" +
                "  <tr><td>✅ <b>协议支持</b></td><td>(IAC 协议初步支持)</td></tr>" +
                "  <tr><td>✅ <b>多线程模型</b></td><td>(线程与消息模型持续优化收敛)</td></tr>" +
                "  <tr><td>✅ <b>核心系统</b></td><td>(Trigger 触发器、UI 界面支持)</td></tr>" +
                "  <tr><td>✅ <b>高级渲染</b></td><td>(ANSI 颜色渲染、基于 GMCP 的人物信息)</td></tr>" +
                "  <tr><td>⏳ <b>进行中功能</b></td><td>(Alias 系统、Timer 系统暂停后持续推进中)</td></tr>" +
                "  <tr><td>❌ <b>未完成功能</b></td><td>(地图房间动态绘制)</td></tr>" +
                "</table>" +
                "</html>";
        return createHtmlScrollPane(html);
    }

    /**
     * 创建"运行说明"面板
     */
    private JComponent createRunPanel() {
        String html = "<html>" +
                "<h2>🚀 运行与配置方式</h2>" +
                "<p>项目支持通过 <b>YAML</b> 文件进行基础配置（MUD 服务器的 Host 与 Port 等）：</p>" +
                "<pre style='background-color:#F5F5F5; padding:5px;'>" +
                "mud:\n" +
                "  server:\n" +
                "    host: xxx.xxx.xxx.xxx\n" +
                "    port: 4000" +
                "</pre>" +
                "<h3>🛠️ 编译与运行：</h3>" +
                "<p><b>方式一：</b>使用 Maven 编译构建并运行 Jar</p>" +
                "<code>mvn clean install</code><br>" +
                "<code>java -jar zm-mud.jar</code>" +
                "<br><br>" +
                "<p><b>方式二：</b>在 IDE 中直接运行 <code>ZmMud.java</code> 中的 <code>main</code> 方法。</p>" +
                "</html>";
        return createHtmlScrollPane(html);
    }

    /**
     * 辅助工具方法：将 HTML 字符串封装进不可编辑的 JTextPane 并置于 JScrollPane 中
     */
    private JScrollPane createHtmlScrollPane(String htmlContent) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(htmlContent);
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        // 处理超链接点击样式，使其更符合用户习惯（若需实际跳转功能，可添加 HyperlinkListener）
        textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return scrollPane;
    }

    @Override
    protected void ok() {
        // 点击确定或关闭时隐藏/销毁弹窗
        logger.debug("关闭帮助对话框");
        dispose();
    }
}

package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import zm.mud.core.api.ClientService;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.utils.SpringBeanUtil;

public class MudMainScreen extends JFrame {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudMainScreen.class);

    private MudTextAare mudTextAare;

    private MudInputField mudInputField;

    private GlobleCfg globleCfg;

    private ZmMudUI ui;

    public MudMainScreen(GlobleCfg cfg,ZmMudUI ui) {
        this.globleCfg = cfg;
        this.ui = ui;
        setTitle(this.globleCfg.getTitle());
        setSize(new Dimension(this.globleCfg.getWidth(), this.globleCfg.getHeight())); 

        // 1. ✨ 必须改成 DO_NOTHING，把红叉的控制权拿回手里
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
    }

    private void init() {
        setLayout(new BorderLayout());

        // 设置文本区固定高度
        this.mudTextAare = new MudTextAare(this.globleCfg);
        mudTextAare.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height - 30));
        JScrollPane scrollPane = new JScrollPane(mudTextAare);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(mudTextAare.getPreferredSize()); // 高度固定150px

        add(scrollPane, BorderLayout.CENTER);

        // 输入框在底部
        this.mudInputField = new MudInputField(this.ui);
        add(this.mudInputField, BorderLayout.SOUTH);

    }

    public void printlnToScreen(String text,boolean enableBlod) {
        this.mudTextAare.printlnToScreen(text,enableBlod);
        
    }
    public int getMsgOffset(String msg){
        return this.mudTextAare.getMsgOffset(msg);
    }

    /**
     * @see MudTextAare#printImg(String, int)
     * @param imgUrl
     * @param offset
     */
    public void printImg(String imgUrl,int offset,boolean insertMode) {
        this.mudTextAare.printImg(imgUrl,offset,insertMode);
    }

    public void setShow() {
        this.mudTextAare.setVisible(true);
        this.mudInputField.setVisible(true);
        this.setVisible(true);
        this.mudInputField.requestFocusInWindow();
    }

    public void resetFont(String font,int size) {
        this.mudTextAare.setFont(new Font(font, Font.PLAIN, size));
    }
}

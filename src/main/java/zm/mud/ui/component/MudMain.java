package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class MudMain extends JFrame {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudMain.class);

    private MudTextAare mudTextAare;

    private MudInputField mudInputField;

    public MudMain(String title, int width, int height) {
        setTitle(title);
        setSize(new Dimension(width, height)); 

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.init();
        pack(); // 根据组件首选大小调整窗口
        setLocationRelativeTo(null); 
    }

    private void init() {
        setLayout(new BorderLayout());

        // 设置文本区固定高度
        this.mudTextAare = new MudTextAare();
        mudTextAare.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height - 30));
        JScrollPane scrollPane = new JScrollPane(mudTextAare);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(mudTextAare.getPreferredSize()); // 高度固定150px

        add(scrollPane, BorderLayout.CENTER);

        // 输入框在底部
        this.mudInputField = new MudInputField();
        add(this.mudInputField, BorderLayout.SOUTH);

    }

    public void printlnToScreen(String text) {
        this.mudTextAare.printlnToScreen(text);
        ;
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

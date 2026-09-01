package zm.mud.ui.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import zm.mud.core.session.MudSession;
import zm.mud.ui.theme.Dark;
import zm.mud.ui.theme.ITheme;

// 改为继承 JPanel，作为复合组件
public class MudInputField extends JPanel implements IMudUiComponent {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudInputField.class);

    private static final int MAX_HISTORY_COUNT = 10;
    private static final String DEFAULT_USER_NAME = "无名侠";

    private List<String> history;
    private int maxHistoryCnt;
    private int currentHistoryIndex;
    private MudSession session;
    private MudTabPanel currentTabPanel;

    // 左侧的用户名标签
    private JLabel nameLabel;
    // 内部真正的文本输入框（原本属于 this 的方法和属性转移到它身上）
    private JTextField textField;

    public MudInputField(MudSession session, MudTabPanel currentTabPanel) {
        this.session = session;
        this.currentTabPanel = currentTabPanel;
   
        // 初始化外层面板的布局为边界布局
        this.setLayout(new BorderLayout());

        // 初始化左侧标签（假设可以通过 session 获取到用户 name，这里暂用 session.toString() 示例，可根据实际 API 修改）
        this.nameLabel = new JLabel();
        this.nameLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
        this.setName(String.format("[%s]: ", DEFAULT_USER_NAME)); 
        // 初始化真正的输入框
        this.textField = new JTextField();
        this.textField.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14)); // 强制等宽，与MUD对齐

        // 将组件放入面板：标签在左，输入框在中间（自动撑满剩余空间）
        this.add(this.nameLabel, BorderLayout.WEST);
        this.add(this.textField, BorderLayout.CENTER);

        this.initHistoryRelated();
        this.initEnterAction();
        //用于实现输入框的历史记录上下翻阅
        this.initKeyBindings();

        this.applyTheme(null);

        // 3. 核心：将边框和顶部悬空分割线应用到外层面板（JPanel）上，保持整体视觉效果
        this.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(70, 70, 70)), // 顶部有一条精致的灰色分割线
            javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10) // 上下内边距 6 像素，左右 10 像素
        ));
    }

    // 可自定义处理输入
    private void handleInput(String input) {
        this.session.send(input);
    }

    private void initHistoryRelated() {
        this.history = new ArrayList<>();
        this.maxHistoryCnt = MAX_HISTORY_COUNT;
        this.currentHistoryIndex = 0;
    }

    private void initEnterAction() {
        // 按回车触发事件（作用于 textField）
        this.textField.addActionListener(e -> {
            String input = this.textField.getText().trim(); // 去掉首尾空格
            if (!input.isEmpty()) {
                this.textField.setText("");
                this.pushToHistory(input);
                this.showCurrentInput(input);
                handleInput(input);
            }
        });
    }

    /**
     * 用于回显命令
     * @param input
     */
    private void showCurrentInput(String input){
        this.currentTabPanel.printlnToScreen("> " + input);
    }

    private void pushToHistory(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        if (this.history.size() >= this.maxHistoryCnt) {
            this.history.remove(0);
        }
        this.history.add(input);
        this.currentHistoryIndex = this.history.size();
    }

    private void initKeyBindings() {
        // 1. 获取输入映射（InputMap）和动作映射（ActionMap）
        // 作用于 textField
        this.textField.getInputMap(JTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "historyUp");
        this.textField.getInputMap(JTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "historyDown");

        // 2. 绑定“向上按键”触发的动作
        this.textField.getActionMap().put("historyUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleArrowKey(true);
            }
        });

        // 3. 绑定“向下按键”触发的动作
        this.textField.getActionMap().put("historyDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleArrowKey(false);
            }
        });
    }

    private void handleArrowKey(boolean isUp) {
        if (this.history == null || this.history.isEmpty()) {
            return;     // elary end point
        }

        if (isUp) {
            boolean currentIsHead = this.currentHistoryIndex == 0;
            if (currentIsHead) {
                return; // elary end point
            }

            this.currentHistoryIndex--;
            if (this.currentHistoryIndex < 0) {
                this.currentHistoryIndex = 0;
            }
        } else {
            boolean currentIsTail = this.currentHistoryIndex >= this.history.size() - 1;
            if (currentIsTail) {
                this.textField.setText("");
                this.textField.setCaretPosition(this.textField.getText().length());
                this.currentHistoryIndex = this.history.size(); // 此时，应该只想空白处的一个index，实际是一个非法的index
                return; // elary end point
            }
            this.currentHistoryIndex++;
            if (this.currentHistoryIndex > this.maxHistoryCnt) {
                this.currentHistoryIndex = this.maxHistoryCnt;
            }
        }
        String historyStr = this.history.get(currentHistoryIndex);

        if (historyStr == null || historyStr.trim().isEmpty()) {
            return;
        }

        this.textField.setText(historyStr);

        this.textField.setCaretPosition(this.textField.getText().length());
    }

    @Override
    public void applyTheme(ITheme theme) {
        if (theme == null || theme.equals(Dark.INSTANCE)) {
            // ================== UI 视觉优化（默认) ==================
            // 保持整体底色一致
            this.setBackground(new java.awt.Color(30, 30, 30)); 
            
            // 设置输入框与标签的颜色
            this.textField.setBackground(new java.awt.Color(30, 30, 30));
            this.textField.setForeground(java.awt.Color.WHITE); 
            this.textField.setCaretColor(java.awt.Color.GREEN);
            
            this.nameLabel.setForeground(java.awt.Color.CYAN); // 标签用青色区分，或用纯白
        } else {
            this.setBackground(theme.getBackground("40"));
            
            this.textField.setBackground(theme.getBackground("40")); 
            this.textField.setForeground(theme.getForeground("97"));
            this.textField.setCaretColor(java.awt.Color.BLACK);
            
            this.nameLabel.setForeground(theme.getForeground("97"));
        }

        // 去掉输入框自带的边框，使其与外层面板融为一体
        this.textField.setBorder(null);
       
        this.textField.putClientProperty("caretWidth", 2); // 部分 LookAndFeel 支持加粗光标
    }

    public void setName(String name) {
        this.nameLabel.setText(name);
        this.revalidate();
        this.repaint();
    }

        @Override
    public void requestFocus() {
        if (this.textField != null) {
            this.textField.requestFocus();
        } else {
            super.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        if (this.textField != null) {
            return this.textField.requestFocusInWindow();
        }
        return super.requestFocusInWindow();
    }

}

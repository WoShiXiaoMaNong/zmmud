package zm.mud.ui.component;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import zm.mud.core.session.MudSession;
import zm.mud.ui.theme.Dark;
import zm.mud.ui.theme.ITheme;


public class MudInputField extends javax.swing.JTextField implements IMudUiComponent{
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudInputField.class);

    private static final int MAX_HISTORY_COUNT = 10;

    private List<String> history;
    private int maxHistoryCnt;
    private int currentHistoryIndex;
    private MudSession session;
    private MudTabPanel currentTabPanel;

    public MudInputField(MudSession session, MudTabPanel currentTabPanel) {
        this.session = session;

        this.currentTabPanel = currentTabPanel;
   
        this.initHistoryRelated();

        this.initEnterAction();

        //用于实现输入框的历史记录上下翻阅
        this.initKeyBindings();

        this.applyTheme(null);
        this.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14)); // 强制等宽，与MUD对齐
        // 3. 核心：增加边框和顶部悬空分割线（让它与主文本区彻底分离）
        // LineBorder(Color.GRAY, 1) 提供一个浅灰色外圈
        // EmptyBorder(6, 10, 6, 10) 给输入框内部文字四周留出空白，不再紧贴边框
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
        // 按回车触发事件
        addActionListener(e -> {
            String input = getText().trim(); // 去掉首尾空格
            if (!input.isEmpty()) {
                setText("");
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
        // WHEN_FOCUSED 表示只有当输入框获得焦点时才触发
        this.getInputMap(JTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "historyUp");
        this.getInputMap(JTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "historyDown");

        // 2. 绑定“向上按键”触发的动作
        this.getActionMap().put("historyUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleArrowKey(true);
            }
        });

        // 3. 绑定“向下按键”触发的动作
        this.getActionMap().put("historyDown", new AbstractAction() {
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
                this.setText("");
                this.setCaretPosition(this.getText().length());
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

        this.setText(historyStr);

        this.setCaretPosition(this.getText().length());
    }

    @Override
    public void applyTheme(ITheme theme) {
        if (theme == null || theme.equals(Dark.INSTANCE)) {
            // ================== UI 视觉优化（默认) ==================
            // 1. 设置专属的背景色与前景色（确保暗黑主题下的高对比度）
            this.setBackground(new java.awt.Color(30, 30, 30)); // 略深于主屏幕的纯黑，提升层次感
            this.setForeground(java.awt.Color.WHITE); // 纯白文字 // 2. 强化光标（Caret）：改为刺眼的绿色或亮白色，并且变粗，极易捕捉
            this.setCaretColor(java.awt.Color.GREEN);
        } else {
            this.setBackground(theme.getBackground("40")); 
            this.setForeground(theme.getForeground("97"));
            this.setCaretColor(java.awt.Color.BLACK);
        }

       
        this.putClientProperty("caretWidth", 2); // 部分 LookAndFeel 支持加粗光标

    }

}

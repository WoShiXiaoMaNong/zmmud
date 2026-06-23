package zm.mud.ui.component;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import zm.mud.core.api.OubMsgService;
import zm.mud.ui.ZmMudUI;

public class MudInputField extends javax.swing.JTextField {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudInputField.class);

    private static final int MAX_HISTORY_COUNT = 10;

    private List<String> history;
    private int maxHistoryCnt;
    private int currentHistoryIndex;
    private OubMsgService oms;
    private MudMainScreen mainScreen;

    public MudInputField(MudMainScreen mainScreen) {
        this.mainScreen = mainScreen;
        oms = ZmMudUI.getContext().getBean(OubMsgService.class);

        this.initHistoryRelated();

        this.initEnterAction();

        //用于实现输入框的历史记录上下翻阅
        this.initKeyBindings();
    }

    // 可自定义处理输入
    private void handleInput(String input) {
        oms.send(input);
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
        this.mainScreen.printlnToScreen("> " + input);
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

}

package zm.mud.ui.component;

import zm.mud.api.OubMsgQueueService;
import zm.mud.ui.ZmMudUI;

public class MudInputField extends javax.swing.JTextField {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MudInputField.class);

    private OubMsgQueueService oms;
     public MudInputField() {
        // 按回车触发事件
        addActionListener(e -> {
            String input = getText().trim(); // 去掉首尾空格
            if (!input.isEmpty()) {
                setText("");
                handleInput(input);
            }
        });
        oms = ZmMudUI.getContext().getBean(OubMsgQueueService.class);
    }

    // 可自定义处理输入
    private void handleInput(String input) {
        oms.send(input);
    }

}

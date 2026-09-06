package zm.mud.ui.component.menu.setting.trigger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.ui.component.menu.KeyValuePair;

public class ActionConfigView extends JPanel {
    private JComboBox<KeyValuePair<String, String>> typeCombo;
    private JTextArea expressionArea; // 修正：改为多行文本域

    public ActionConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Action 动作器"));
        initViews();
    }
    private void initViews() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 类型 (第 0 行)
        gbc.insets = new Insets(6, 8, 4, 8); // 适当调整间距
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        add(new JLabel("类型:"), gbc);
        
        typeCombo = new JComboBox<>();
        for (KeyValuePair<String, String> pair : TriggerService.getActionTypes()) {
            typeCombo.addItem(pair);
        }
        setupRenderer(typeCombo);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(typeCombo, gbc);

        // 2. 表达式标签 (第 1 行)
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        add(new JLabel("Expression 表达式:"), gbc);

        // 3. 多行输入区域 (第 2 行 - 纵向高度拉满)
        // 💡 核心修改：改为 3 行或 4 行。不要用 8 行去死撑高度，把高度决定权交给父容器的 weighty
        expressionArea = new JTextArea(4, 40); 
        expressionArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(expressionArea);
        
        // 💡 核心修改：下边距（bottom）严格设为 0！防止底部出现 6px 的空白断层
        gbc.insets = new Insets(4, 8, 0, 8); 
        gbc.gridy = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);
    }

    private void setupRenderer(JComboBox<KeyValuePair<String, String>> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KeyValuePair) {
                    setText(((KeyValuePair<String,String>) value).getValue());
                }
                return this;
            }
        });
    }

    public MatcherAndActionConfigEntry getActionData() {
        MatcherAndActionConfigEntry entry = new MatcherAndActionConfigEntry();
        KeyValuePair<String, String> selected = (KeyValuePair<String, String>) typeCombo.getSelectedItem();
        if (selected != null) {
            entry.setType(selected.getKey());
        }
        entry.setExpression(expressionArea.getText());
        entry.setParams(new HashMap<>());
        return entry;
    }

    public void setActionData(MatcherAndActionConfigEntry entry) {
        if (entry == null) {
            typeCombo.setSelectedIndex(0);
            expressionArea.setText("");
            return;
        }
        for (int i = 0; i < typeCombo.getItemCount(); i++) {
            if (typeCombo.getItemAt(i).getKey().equals(entry.getType())) {
                typeCombo.setSelectedIndex(i);
                break;
            }
        }
        expressionArea.setText(entry.getExpression());
    }
}

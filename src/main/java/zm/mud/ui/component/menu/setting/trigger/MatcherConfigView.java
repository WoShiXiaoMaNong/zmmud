package zm.mud.ui.component.menu.setting.trigger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.ui.component.menu.KeyValuePair;

public class MatcherConfigView extends JPanel {
    private JComboBox<KeyValuePair<String, String>> typeCombo;
    private JTextArea expressionArea; // 修正：改为多行文本域

    public MatcherConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Matcher 匹配器"));
        initViews();
    }

    private void initViews() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 类型 (第 0 行)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        add(new JLabel("类型:"), gbc);
        
        typeCombo = new JComboBox<>();
        for (KeyValuePair<String, String> pair : TriggerService.getMathers()) {
            typeCombo.addItem(pair);
        }
        setupRenderer(typeCombo);
        gbc.gridx = 1; gbc.weightx = 1.0;
        add(typeCombo, gbc);

        // 2. 表达式标签 (第 1 行)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        add(new JLabel("Expression 表达式:"), gbc);

        // 3. 多行输入区域 (第 2 行 - 纵向和横向拉满 fill = BOTH)
        expressionArea = new JTextArea(4, 20); // 初始化行数与列数
        expressionArea.setLineWrap(true);       // 自动换行
        JScrollPane scrollPane = new JScrollPane(expressionArea);
        
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

    public MatcherAndActionConfigEntry getMatcherData() {
        MatcherAndActionConfigEntry entry = new MatcherAndActionConfigEntry();
        KeyValuePair<String, String> selected = (KeyValuePair<String, String>) typeCombo.getSelectedItem();
        if (selected != null) {
            entry.setType(selected.getKey());
        }
        entry.setExpression(expressionArea.getText()); // 获取多行文本
        entry.setParams(new HashMap<>());
        return entry;
    }

    public void setMatcherData(MatcherAndActionConfigEntry entry) {
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

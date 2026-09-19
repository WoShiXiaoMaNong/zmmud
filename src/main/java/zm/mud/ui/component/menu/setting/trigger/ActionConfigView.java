package zm.mud.ui.component.menu.setting.trigger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.ui.component.menu.KeyValuePair;

public class ActionConfigView extends JPanel {
    private JComboBox<KeyValuePair<String, String>> typeCombo;
    private JTextArea expressionArea; 

    private JPanel paramsPanel;
    private java.util.List<ParamRow> paramRows = new java.util.ArrayList<>();

    public ActionConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Action 动作器"));
        initViews();
    }

    private void initViews() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 类型 (第 0 行)
        gbc.insets = new Insets(6, 8, 4, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        add(new JLabel("类型:"), gbc);

        typeCombo = new JComboBox<>();
        for (KeyValuePair<String, String> pair : TriggerService.getActionTypes()) {
            typeCombo.addItem(pair);
        }
        setupRenderer(typeCombo);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(typeCombo, gbc);

        // 2. Params 维护区域标签与容器 (第 1 行)
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        add(new JLabel("Params 参数:"), gbc);

        // 固定高度 + 滚动条 + 紧凑按钮
        JPanel paramsContainer = new JPanel(new BorderLayout(5, 5));
        
        // 使用 BoxLayout 垂直排列参数行
        paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.Y_AXIS));
        
        // 放入滚动面板，并强制限制首选高度（例如 120 像素）
        JScrollPane paramsScrollPane = new JScrollPane(paramsPanel);
        paramsScrollPane.setPreferredSize(new Dimension(100, 60)); 
        paramsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        paramsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 创建一个小巧、左对齐的添加按钮
        JButton addParamBtn = new JButton("+ 添加参数");
        addParamBtn.setMargin(new Insets(2, 8, 2, 8)); // 缩小按钮内边距
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // 让按钮靠左，不拉伸
        btnWrapper.add(addParamBtn);

        addParamBtn.addActionListener(e -> addParamRow("", ""));

        paramsContainer.add(paramsScrollPane, BorderLayout.CENTER);
        paramsContainer.add(btnWrapper, BorderLayout.SOUTH);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(paramsContainer, gbc);

        // 3. 表达式标签 (第 2 行)
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(new JLabel("Expression 表达式:"), gbc);

        // 4. 多行输入区域 (第 3 行 - 纵向高度拉满)
        expressionArea = new JTextArea(4, 40);
        expressionArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(expressionArea);

        gbc.insets = new Insets(4, 8, 6, 8);
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);
    }

    // 动态添加一行参数输入
    private void addParamRow(String key, Object value) {
        ParamRow row = new ParamRow();
        row.keyField.setText(key);
        row.valueField.setText(value != null ? value.toString() : "");
        paramRows.add(row);
        paramsPanel.add(row.rowPanel);

        // 刷新 UI 并滚动到底部
        paramsPanel.revalidate();
        paramsPanel.repaint();
        
        // 延迟滚动，确保新组件布局完成后再执行滚动
        SwingUtilities.invokeLater(() -> {
            row.rowPanel.scrollRectToVisible(row.rowPanel.getBounds());
        });
    }

    // 动态删除一行参数
    private void removeParamRow(ParamRow row) {
        paramRows.remove(row);
        paramsPanel.remove(row.rowPanel);

        // 刷新 UI
        paramsPanel.revalidate();
        paramsPanel.repaint();
    }

    public void setActionData(MatcherAndActionConfigEntry entry) {
        // 清空旧的 params 行
        paramRows.clear();
        paramsPanel.removeAll();

        if (entry == null) {
            typeCombo.setSelectedIndex(0);
            expressionArea.setText("");
            paramsPanel.revalidate();
            paramsPanel.repaint();
            return;
        }

        // 回填类型
        for (int i = 0; i < typeCombo.getItemCount(); i++) {
            if (typeCombo.getItemAt(i).getKey().equals(entry.getType())) {
                typeCombo.setSelectedIndex(i);
                break;
            }
        }

        // 循环回填 Map 中的数据到 paramsPanel 视图中
        Map<String, Object> params = entry.getParams();
        if (params != null) {
            for (Map.Entry<String, Object> item : params.entrySet()) {
                addParamRow(item.getKey(), item.getValue());
            }
        }

        expressionArea.setText(entry.getExpression());

        paramsPanel.revalidate();
        paramsPanel.repaint();
    }

    private void setupRenderer(JComboBox<KeyValuePair<String, String>> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KeyValuePair) {
                    setText(((KeyValuePair<String, String>) value).getValue());
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
        entry.setParams(this.getParamsData());
        return entry;
    }

    public Map<String, Object> getParamsData() {
        Map<String, Object> result = new HashMap<>();
        for (ParamRow row : paramRows) {
            String key = row.keyField.getText().trim();
            String value = row.valueField.getText().trim();
            if (!key.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }

    // 💡 优化内部类：微调输入框高度与间距，使多行排列时看起来更整洁
    private class ParamRow {
        JTextField keyField = new JTextField(10);
        JTextField valueField = new JTextField(15);
        JButton deleteBtn = new JButton("X");
        JPanel rowPanel;

        ParamRow() {
            // 设置微小的上下间距 (2 像素)，左右间距保持 5 像素
            rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            rowPanel.add(new JLabel("键:"));
            rowPanel.add(keyField);
            rowPanel.add(new JLabel("值:"));
            rowPanel.add(valueField);

            deleteBtn.setMargin(new Insets(1, 4, 1, 4));
            deleteBtn.addActionListener(e -> removeParamRow(this));
            rowPanel.add(deleteBtn);
            
            // 确保 BoxLayout 垂直排列时不会把单行强行拉高
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowPanel.getPreferredSize().height));
        }
    }
}

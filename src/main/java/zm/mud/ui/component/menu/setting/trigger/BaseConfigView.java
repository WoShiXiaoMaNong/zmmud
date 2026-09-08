package zm.mud.ui.component.menu.setting.trigger;
import javax.swing.*;
import java.awt.*;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.ui.component.menu.KeyValuePair;

public class BaseConfigView extends JPanel {
    private JTextField nameField = new JTextField();
    private JComboBox<KeyValuePair<String, String>> typeCombo = new JComboBox<>();
    private JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(-1, -1, 999999, 1));
    private JCheckBox cbSync , cbUnique , cbAutoRegister;

    public BaseConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Trigger 基础配置"));
        initViews();
    }

    private void initViews() {
        // 1. Sync
        cbSync = new JCheckBox("同步阻塞");
        cbSync.setToolTipText("触发器将在主线程中同步运行，完成后才处理下一步的输入输出消息。当需要严格顺序是，开启");

        // 2. Unique
        cbUnique = new JCheckBox("全局唯一");
        cbUnique.setToolTipText("当前会话下全局唯一，同一时间最多只允许出现一个相同的触发器。");

        // 3. Auto Register
        cbAutoRegister = new JCheckBox("自动注册");
        cbAutoRegister.setToolTipText("会话（Session）启动时，自动将该触发器注册到运行时环境中。");


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8); gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 名称 (第 0 行)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; add(new JLabel("名称:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; add(nameField, gbc);

        // 2. Trigger 类型 (第 1 行)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; add(new JLabel("Trigger 类型:"), gbc);
        for (KeyValuePair<String, String> pair : TriggerService.getTriggerTypes()) typeCombo.addItem(pair);
        setupKeyValuePairRenderer(typeCombo);
        gbc.gridx = 1; gbc.weightx = 1.0; add(typeCombo, gbc);

        // 3. 执行次数 (第 2 行)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; add(new JLabel("可触发次数:"), gbc);
        countSpinner.setToolTipText("负数表示无限次数");
        gbc.gridx = 1; gbc.weightx = 1.0; add(countSpinner, gbc);

        // 4. 选项复选框 (第 3 行)
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnl.add(cbSync); pnl.add(cbUnique); pnl.add(cbAutoRegister);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(10, 4, 4, 4);
        add(pnl, gbc);
    }

    private void setupKeyValuePairRenderer(JComboBox<KeyValuePair<String, String>> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof KeyValuePair) setText(((KeyValuePair<String, String>) v).getValue());
                return this;
            }
        });
    }

    public void getBaseData(TriggerConfigEntry entry) {
        entry.setName(nameField.getText().trim());
        KeyValuePair<String, String> sel = (KeyValuePair<String, String>) typeCombo.getSelectedItem();
        if (sel != null) entry.setType(sel.getKey());
        entry.setRemainingCount((Integer) countSpinner.getValue());
        entry.setSync(cbSync.isSelected());
        entry.setUnique(cbUnique.isSelected());
        entry.setAutoRegister(cbAutoRegister.isSelected());
    }

    public void setBaseData(TriggerConfigEntry entry) {
        boolean isNull = (entry == null);
        nameField.setText(isNull ? "" : entry.getName());
        countSpinner.setValue(isNull ? -1 : (entry.getRemainingCount() != null ? entry.getRemainingCount() : -1));
        cbSync.setSelected(!isNull && entry.isSync());
        cbUnique.setSelected(!isNull && entry.isUnique());
        cbAutoRegister.setSelected(!isNull && entry.isAutoRegister());
        
        typeCombo.setSelectedIndex(0);
        if (!isNull) {
            for (int i = 0; i < typeCombo.getItemCount(); i++) {
                if (typeCombo.getItemAt(i).getKey().equals(entry.getType())) { typeCombo.setSelectedIndex(i); break; }
            }
        }
    }
}

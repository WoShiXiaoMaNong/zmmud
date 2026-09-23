package zm.mud.ui.component.menu.setting.timer;

import javax.swing.*;
import java.awt.*;
import zm.mud.core.automation.timer.cfg.TimerConfigEntry;

/**
 * 定时器基础属性配置面板 (名称、可触发次数、启用状态)
 * 严格对齐官方定义的 TimerConfigEntry 结构
 */
public class TimerBaseConfigView extends JPanel {
    private JTextField nameField = new JTextField();
    private JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(-1, -1, 999999, 1));
    private JCheckBox cbEnable;

    public TimerBaseConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Timer 基础配置"));
        initViews();
    }

    private void initViews() {
        cbEnable = new JCheckBox("启用");
        cbEnable.setToolTipText("勾选以激活该定时器，使其投入运行时环境。");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 名称 (第 0 行)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; add(new JLabel("名称:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; add(nameField, gbc);

        // 2. 执行次数 (第 1 行)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; add(new JLabel("可触发次数:"), gbc);
        countSpinner.setToolTipText("负数表示无限次数");
        gbc.gridx = 1; gbc.weightx = 1.0; add(countSpinner, gbc);

        // 3. 启用选项复选框 (第 2 行)
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnl.add(cbEnable);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0; 
        gbc.insets = new Insets(12, 4, 4, 4);
        add(pnl, gbc);
    }

    /**
     * 【严格匹配】将 UI 数据收集并写入官方定义的实体类
     */
    public void getBaseData(TimerConfigEntry entry) {
        if (entry == null) return;
        
        entry.setName(nameField.getText().trim());
        entry.setRemainingCount((Integer) countSpinner.getValue());
        entry.setEnable(cbEnable.isSelected()); // 对接原生 boolean 的 setEnable 方法
    }

    /**
     * 【严格匹配】将官方实体类的数据安全回显到 UI 界面
     */
    public void setBaseData(TimerConfigEntry entry) {
        boolean isNull = (entry == null);
        
        nameField.setText(isNull ? "" : entry.getName());
        
        // 读取并回显 remainingCount 计数值，若为空则默认给 -1 (表示无限次)
        countSpinner.setValue(isNull ? -1 : (entry.getRemainingCount() != null ? entry.getRemainingCount() : -1));
        
        // 读取并回显原生 boolean 类型的 isEnable() 状态
        cbEnable.setSelected(!isNull && entry.isEnable());
    }
}

package zm.mud.ui.component.menu.setting.timer;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import zm.mud.core.automation.timer.cfg.TimerConfigEntry;
import zm.mud.ui.component.menu.KeyValuePair;

/**
 * 定时器触发策略配置面板 (定点触发/延时启动)
 * 严格对齐 JSON 数据模型中 type 和 trigger_value 的动态读写
 * 【智能美化版】定点触发采用独立时分秒框，且无配置或新增时，默认自动初始化为系统当前时间
 */
public class TimerPolicyConfigView extends JPanel {
    private JComboBox<KeyValuePair<String, String>> typeCombo = new JComboBox<>();
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Card 1: 延时启动组件（毫秒控制）
    private JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(1000, 1, 3600000, 100)); // 默认1000ms，步长100ms

    // Card 2: 定点触发美化组件（时、分、秒独立框）
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;

    private final String CARD_DELAY = "DELAY";
    private final String CARD_CRON = "CRON";

    public TimerPolicyConfigView() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Timer 策略配置"));
        initViews();
    }

    private void initViews() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 4, 8); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 定时器类型 (第 0 行)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; add(new JLabel("定时器类型:"), gbc);
        
        typeCombo.addItem(new KeyValuePair<>(CARD_DELAY, "延时启动 (Delay)"));
        typeCombo.addItem(new KeyValuePair<>(CARD_CRON, "定点触发 (Time)"));
        setupRenderer(typeCombo);
        
        gbc.gridx = 1; gbc.weightx = 1.0; add(typeCombo, gbc);

        // 2. 动态卡片容器 (第 1 行)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // ─── 初始化卡片 1：延时面板 ───
        JPanel delayCard = new JPanel(new GridBagLayout());
        GridBagConstraints dGbc = new GridBagConstraints();
        dGbc.fill = GridBagConstraints.HORIZONTAL; 
        dGbc.insets = new Insets(2, 0, 2, 0);
        dGbc.gridx = 0; dGbc.gridy = 0; dGbc.weightx = 0.0; delayCard.add(new JLabel("延时(毫秒):"), dGbc);
        dGbc.gridx = 1; dGbc.weightx = 1.0; delayCard.add(delaySpinner, dGbc);

        // ─── 初始化卡片 2：美化版时分秒独立分框面板 ───
        JPanel cronCard = new JPanel(new GridBagLayout());
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.fill = GridBagConstraints.HORIZONTAL; 
        cGbc.insets = new Insets(2, 0, 2, 0);
        cGbc.gridx = 0; cGbc.gridy = 0; cGbc.weightx = 0.0; cronCard.add(new JLabel("触发时间点:"), cGbc);
        
        // 核心美化：时分秒横向排布容器
        JPanel hmsWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        
        // 实例化三个独立的数字微调器，并开启循环滚动模式
        hourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        secondSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        // 强制约束每个独立输入框的尺寸，确保等宽对称
        Dimension spinnerSize = new Dimension(45, 24);
        hourSpinner.setPreferredSize(spinnerSize);
        minuteSpinner.setPreferredSize(spinnerSize);
        secondSpinner.setPreferredSize(spinnerSize);

        // 格式化文本：去除千分位，文字居中
        formatSpinnerEditor(hourSpinner);
        formatSpinnerEditor(minuteSpinner);
        formatSpinnerEditor(secondSpinner);

        // 顺次拼装到面板，并用等宽加粗冒号隔开
        hmsWrapperPanel.add(hourSpinner);
        hmsWrapperPanel.add(createColonLabel());
        hmsWrapperPanel.add(minuteSpinner);
        hmsWrapperPanel.add(createColonLabel());
        hmsWrapperPanel.add(secondSpinner);
        
        cGbc.gridx = 1; cGbc.weightx = 1.0; cronCard.add(hmsWrapperPanel, cGbc);

        // 添加入卡片系统
        cardPanel.add(delayCard, CARD_DELAY);
        cardPanel.add(cronCard, CARD_CRON);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH; 
        gbc.insets = new Insets(4, 8, 4, 8);
        add(cardPanel, gbc);

        // 联动卡片切换事件
        typeCombo.addActionListener(e -> {
            KeyValuePair<String, String> sel = (KeyValuePair<String, String>) typeCombo.getSelectedItem();
            if (sel != null) {
                cardLayout.show(cardPanel, sel.getKey());
            }
        });
    }

    private void formatSpinnerEditor(JSpinner spinner) {
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "00");
        JFormattedTextField textField = editor.getTextField();
        textField.setHorizontalAlignment(JTextField.CENTER);
        spinner.setEditor(editor);
    }

    private void createCurrentTimeDefault() {
        Calendar cal = Calendar.getInstance();
        updateHmsSpinners(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND)
        );
    }

    private JLabel createColonLabel() {
        JLabel label = new JLabel(" : ");
        label.setFont(new Font("Consolas", Font.BOLD, 14)); 
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    /**
     * 收集界面数据持久化回写至实体类
     */
    public void getPolicyData(TimerConfigEntry entry) {
        if (entry == null) return;
        
        KeyValuePair<String, String> sel = (KeyValuePair<String, String>) typeCombo.getSelectedItem();
        if (sel != null) {
            entry.setType(sel.getKey().toLowerCase()); 
        }
        
        if ("delay".equals(entry.getType())) {
            entry.setTriggerValue(String.valueOf(delaySpinner.getValue()));
        } else {
            int h = (Integer) hourSpinner.getValue();
            int m = (Integer) minuteSpinner.getValue();
            int s = (Integer) secondSpinner.getValue();
            entry.setTriggerValue(String.format("%02d:%02d:%02d", h, m, s));
        }
    }

    /**
     * 数据解包：将从 JSON 中读出的数据分发到对应的框内（若值缺失，智能初始化为当前系统时间）
     */
    public void setPolicyData(TimerConfigEntry entry) {
        if (entry == null) {
            typeCombo.setSelectedIndex(0);
            delaySpinner.setValue(1000);
            createCurrentTimeDefault(); // 【智能升级】空数据防御性回滚至当前时间
            cardLayout.show(cardPanel, CARD_DELAY);
            return;
        }

        String typeKey = entry.getType() != null ? entry.getType().toUpperCase() : CARD_DELAY;
        
        if (CARD_DELAY.equals(typeKey)) {
            typeCombo.setSelectedIndex(0);
            cardLayout.show(cardPanel, CARD_DELAY);
            try {
                delaySpinner.setValue(Integer.parseInt(entry.getTriggerValue()));
            } catch (Exception e) {
                delaySpinner.setValue(1000);
            }
            // 延时策略下，顺便把另一个卡片的时分秒重置为当前系统时间，方便玩家切换过去时体验最佳
            createCurrentTimeDefault(); 
        } else {
            typeCombo.setSelectedIndex(1);
            cardLayout.show(cardPanel, CARD_CRON);
            
            // 解析类似 "12:35:08" 的字符串数据并派送
            if (entry.getTriggerValue() == null || entry.getTriggerValue().isEmpty()) {
                createCurrentTimeDefault(); // 【智能升级】无具体值时默认展示当前时间
            } else {
                try {
                    String[] parts = entry.getTriggerValue().split(":");
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    int second = Integer.parseInt(parts[2]);
                    updateHmsSpinners(hour, minute, second);
                } catch (Exception e) {
                    createCurrentTimeDefault(); // 解析错乱时同样安全降级回系统当前时间
                }
            }
        }
    }

    private void updateHmsSpinners(int h, int m, int s) {
        hourSpinner.setValue(Math.min(Math.max(h, 0), 23));
        minuteSpinner.setValue(Math.min(Math.max(m, 0), 59));
        secondSpinner.setValue(Math.min(Math.max(s, 0), 59));
    }

    private void setupRenderer(JComboBox<KeyValuePair<String, String>> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof KeyValuePair) {
                    setText(((KeyValuePair<String, String>) v).getValue());
                }
                return this;
            }
        });
    }
}

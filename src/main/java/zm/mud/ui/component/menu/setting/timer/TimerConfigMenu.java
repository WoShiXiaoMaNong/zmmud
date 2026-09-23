package zm.mud.ui.component.menu.setting.timer;

import java.awt.*;
import javax.swing.*;
import zm.mud.ui.component.menu.AbsZmMudDialog;
import zm.mud.ui.component.menu.KeyValuePair;
import zm.mud.ui.component.menu.setting.timer.TimerPresenter;
import zm.mud.ui.component.menu.setting.trigger.ActionConfigView;
import zm.mud.ui.component.menu.setting.timer.TimerBaseConfigView;
import zm.mud.ui.component.menu.setting.timer.TimerPolicyConfigView;
import zm.mud.ui.component.menu.setting.timer.TimerListView;
import zm.mud.ui.component.menu.setting.timer.TimerMonitorView;

import java.util.List;

/**
 * 定时器配置主对话框窗口（严格对齐 Trigger UI 规范与分辨率）
 */
public class TimerConfigMenu extends AbsZmMudDialog {

    private JComboBox<KeyValuePair<String,String>> mudConfigCombo; // 对应顶部的“选择配置”下拉框

    private TimerListView listView;
    private TimerBaseConfigView baseConfigView;
    private TimerPolicyConfigView policyConfigView;
    private ActionConfigView actionConfigView;
    private TimerMonitorView monitorView; // 底部定时器监控器
    private TimerPresenter presenter;

    /**
     * 严格对齐基类与 Trigger 的构造函数签名与尺寸
     */
    public TimerConfigMenu(Frame owner, String title) {
        super(owner, title);
        // 保持与 TriggerConfigMenu 相同的标准大窗尺寸
        this.setSize(950, 750); // 考虑到增加了底部的监控器，纵向微调至 750
        // 强行锁死窗口，严禁玩家手动调整大小
        this.setResizable(false);
        this.setLocationRelativeTo(owner);
    }

    /**
     * 核心内容面板拼装（由父类构造函数自发回调）
     */
    @Override
    protected JPanel getContentPanelUi() {
        // 1. 实例化拆分后的原子定时器 UI 组件
        listView = new TimerListView();
        baseConfigView = new TimerBaseConfigView();       // 包含：名称、是否启用
        policyConfigView = new TimerPolicyConfigView();   // 包含：定时器类型下拉(定点/延时)、启动时间/延时秒数输入框
        actionConfigView = new ActionConfigView();       // 包含：动作器类型、动作内容文本域
        monitorView = new TimerMonitorView();             // 包含：表格（是否启用、名称、启动时间、下一次触发时间、运行时长/倒计时）

        // 2. 构建大容器
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.BOTH;

        // ─── 布局一：顶部 MUD 配置栏 (第 0 行) ───
        JPanel topConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topConfigPanel.setBorder(BorderFactory.createTitledBorder("MUD 配置"));
        topConfigPanel.add(new JLabel("选择配置:"));

        mudConfigCombo = new JComboBox<>();
        List<KeyValuePair<String,String>> mudWorldList = TimerService.getMudWorlds();

        if(mudWorldList != null){
            for(KeyValuePair<String,String> mudWorld : mudWorldList){
                mudConfigCombo.addItem(mudWorld);
            }
        }
        topConfigPanel.add(mudConfigCombo);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; 
        mainPanel.add(topConfigPanel, gbc);

        // ─── 布局二：下半部分核心工作区 (第 1 行) ───
        // [左侧] 定时器列表面板
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0; 
        gbc.weighty = 2.0; // 上部区域拉伸权重
        gbc.insets = new Insets(0, 8, 0, 4);
        mainPanel.add(listView, gbc);

        // [右侧] 详细配置编辑网格
        JPanel rightEditPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rGbc = new GridBagConstraints();
        rGbc.fill = GridBagConstraints.BOTH;
        rGbc.insets = new Insets(0, 4, 0, 4);

        // [中上左] 基础配置
        rGbc.gridx = 0;
        rGbc.gridy = 0;
        rGbc.weightx = 0.5;
        rGbc.weighty = 1.0; 
        rGbc.insets = new Insets(0, 4, 6, 4);
        rightEditPanel.add(baseConfigView, rGbc);

        // [中上右] 策略/时间配置
        rGbc.gridx = 1;
        rGbc.gridy = 0;
        rGbc.weightx = 0.5;
        rGbc.weighty = 1.0; 
        rGbc.insets = new Insets(0, 4, 6, 4);
        rightEditPanel.add(policyConfigView, rGbc);

        // [中下] 动作器面板 (强势纵向压缩上方)
        rGbc.gridx = 0;
        rGbc.gridy = 1;
        rGbc.gridwidth = 2; 
        rGbc.weightx = 1.0;
        rGbc.weighty = 4.0; // 吃掉绝大部分垂直空间
        rGbc.insets = new Insets(0, 4, 0, 4);
        rightEditPanel.add(actionConfigView, rGbc);

        rGbc.gridwidth = 1; // 重置

        // 将右侧工作区放入主容器
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 2.0;
        gbc.insets = new Insets(0, 4, 0, 8);
        mainPanel.add(rightEditPanel, gbc);

        // ─── 布局三：底部定时器监控器 (第 2 行，横跨全屏 gridwidth = 2) ───
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.5; // 给底部监控表格提供稳健的独立展示空间
        gbc.insets = new Insets(8, 8, 4, 8); // top=8 撑开与上方配置区的距离
        mainPanel.add(monitorView, gbc);

        // 3. 初始化控制中枢 Presenter
        this.presenter = new TimerPresenter(listView, baseConfigView, policyConfigView, actionConfigView, monitorView);

        // 视图初始化加载逻辑
        mainPanel.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && mainPanel.isShowing()) {
                    if (mudConfigCombo.getItemCount() > 0 && mudConfigCombo.getItemAt(0) != null) {
                        mudConfigCombo.setSelectedIndex(0);
                        KeyValuePair<String,String> firstItem = mudConfigCombo.getItemAt(0);
                        presenter.loadWorldTimers(firstItem.getKey());
                    }
                    mainPanel.removeHierarchyListener(this);
                }
            }
        });

        // 联动顶栏切换
        mudConfigCombo.addActionListener(e -> {
            KeyValuePair<String,String> mudWorld = (KeyValuePair<String,String>) mudConfigCombo.getSelectedItem();
            presenter.loadWorldTimers(mudWorld.getKey());
        });

        return mainPanel;
    }

    @Override
    protected void ok() {
        if (presenter != null) {
            presenter.saveCurrentTimer();
        }
    }
}

package zm.mud.ui.component.menu.setting;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.ui.component.menu.AbsZmMudDialog;
import zm.mud.ui.component.menu.setting.trigger.TriggerPresenter;
import zm.mud.utils.SpringBeanUtil;
import zm.mud.ui.component.menu.setting.trigger.ActionConfigView;
import zm.mud.ui.component.menu.setting.trigger.BaseConfigView;
import zm.mud.ui.component.menu.setting.trigger.MatcherConfigView;
import zm.mud.ui.component.menu.setting.trigger.TriggerListView;

/**
 * 触发器配置主对话框窗口
 * 职责：通过全局 GridBagLayout 强行锁死左侧列表与右侧 Action 的底边对齐，极度压缩行间距
 */
public class Trigger extends AbsZmMudDialog {

    private JComboBox<String> mudConfigCombo; // 对应顶部的“选择配置”下拉框

    private TriggerListView listView;
    private BaseConfigView baseConfigView;
    private MatcherConfigView matcherConfigView;
    private ActionConfigView actionConfigView;
    private TriggerPresenter presenter;

    /**
     * 严格对齐基类与原始 Trigger 的构造函数签名
     */
    public Trigger(Frame owner, String title) {
        super(owner, title);
        // 遵循截图标准：拉宽窗口以容纳横向并排的 BaseConfig 和 MatcherConfig 大文本域
        this.setSize(950, 700);
        // 强行锁死窗口，严禁玩家手动调整大小
        this.setResizable(false);
        this.setLocationRelativeTo(owner);
    }

    /**
     * 核心内容面板拼装（由父类构造函数自发回调）
     * 全局引入 GridBag 约束，消除全部冗余空隙，确保列表底部与 Action 底部像素级绝对齐平
     */
    @Override
    protected JPanel getContentPanelUi() {
        // 1. 实例化拆分后的原子 UI 组件
        listView = new TriggerListView();
        baseConfigView = new BaseConfigView();
        matcherConfigView = new MatcherConfigView();
        actionConfigView = new ActionConfigView();

        // 2. 构建最外层的大容器，采用功能更强大的 GridBagLayout 进行无缝拼接
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8); // 紧凑的全局边缘间距
        gbc.fill = GridBagConstraints.BOTH;

        // ─── 布局一：顶部 MUD 配置栏 (第 0 行，横跨全屏 gridwidth = 2) ───
        JPanel topConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // 内部间距降为 0
        topConfigPanel.setBorder(BorderFactory.createTitledBorder("MUD 配置"));
        topConfigPanel.add(new JLabel("选择配置:"));

        mudConfigCombo = new JComboBox<>(new String[] { "北大侠客行" });
        mudConfigCombo.setSelectedIndex(-1); // 默认不选中
        topConfigPanel.add(mudConfigCombo);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // 顶栏不参与纵向拉伸
        mainPanel.add(topConfigPanel, gbc);

        // ─── 布局二：下半部分核心工作区 ───
        // 为了确保左侧列表底部与右侧 Action 底部绝对齐平，我们把它们放在同一个纵向拉伸层级（gridy = 1）

        // [左侧] 触发器列表面板 (第 1 行，第 0 列) - 独占左侧一整列
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0; // 保持固定合理宽度
        gbc.weighty = 1.0; // 纵向完全拉满，与右侧整体高度严格对齐
        gbc.insets = new Insets(0, 8, 0, 4); // 消除多余间距
        mainPanel.add(listView, gbc);

        // [右侧] 详细配置编辑网格
        JPanel rightEditPanel = new JPanel(new GridBagLayout());
        GridBagConstraints rGbc = new GridBagConstraints();
        rGbc.fill = GridBagConstraints.BOTH;
        rGbc.insets = new Insets(0, 4, 0, 4);

        // 💡【重构核心：上半部分极限压平】
        // 通过设置极低的 weighty (1.0)，配合下方 Action 极高的 weighty (5.0)，
        // 布局管理器会把绝大部分垂直空间压缩给下方的 Action，从而将 Trigger 和 Matcher 强行拍扁

        // [中上左] 基础配置面板 (Trigger 属性)
        rGbc.gridx = 0;
        rGbc.gridy = 0;
        rGbc.gridwidth = 1;
        rGbc.weightx = 0.5;
        rGbc.weighty = 1.0; // 🛠️ 配合下方比例，将其高度占比压缩到最小极限
        rGbc.insets = new Insets(0, 4, 6, 4); // 用 bottom=6 顶开与下方 Action 的间距
        rightEditPanel.add(baseConfigView, rGbc);

        // [中上右] 匹配器面板 (Matcher 属性)
        rGbc.gridx = 1;
        rGbc.gridy = 0;
        rGbc.gridwidth = 1;
        rGbc.weightx = 0.5;
        rGbc.weighty = 1.0; // 🛠️ 保持一致，强制等高
        rGbc.insets = new Insets(0, 4, 6, 4);
        rightEditPanel.add(matcherConfigView, rGbc);

        // 💡【重构核心：下半部分横跨并独占几乎全部空间】
        // [中下] 动作器面板 (Action 动作)
        rGbc.gridx = 0;
        rGbc.gridy = 1;
        rGbc.gridwidth = 2; // 🛠️ 强行横跨，覆盖第 0 列和第 1 列（完全对称横跨 Trigger 和 Matcher）
        rGbc.weightx = 1.0; // 强行横向铺满全部剩余空间
        rGbc.weighty = 5.0; // 🛠️ 极其强势的纵向拉伸权重！吃掉 5/6 的空间，将上方的组件完美压扁，并强行将自身底边向下顶死
        rGbc.insets = new Insets(0, 4, 0, 4); // 底部 Insets 为 0，确保紧贴 rightEditPanel 底边
        rightEditPanel.add(actionConfigView, rGbc);

        rGbc.gridwidth = 1; // 规范操作：重置跨列计数器

        // 将右侧组合体放入主容器
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 4, 0, 8);
        mainPanel.add(rightEditPanel, gbc);

        // 3. 初始化控制中枢 Presenter
        this.presenter = new TriggerPresenter(listView, baseConfigView, matcherConfigView, actionConfigView);

        // 联动顶栏
        mudConfigCombo.addActionListener(e -> {
            String selectedWorld = (String) mudConfigCombo.getSelectedItem();
            if ("北大侠客行".equals(selectedWorld)) {
                presenter.loadWorldTriggers("pkuxkx");
            }
        });

        return mainPanel;
    }

    /**
     * 当玩家点击对话框自带的“确定”按钮时，由父类事件监听器自动回调此方法
     */
    @Override
    protected void ok() {
        if (presenter != null) {
            presenter.saveCurrentTrigger();
        }
    }
}

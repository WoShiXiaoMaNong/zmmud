package zm.mud.ui.component.menu.setting.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import zm.mud.core.automation.timer.cfg.TimerConfigEntry;
import zm.mud.ui.component.menu.setting.trigger.ActionConfigView;

/**
 * 定时器业务中枢 Presenter（全面对接你的 JSON 规范）
 */
public class TimerPresenter {
    private final TimerListView listView;
    private final TimerBaseConfigView baseConfigView;
    private final TimerPolicyConfigView policyConfigView;
    private final ActionConfigView actionConfigView;
    private final TimerMonitorView monitorView;

    private List<TimerConfigEntry> currentTimerList = new ArrayList<>();
    private int currentSelectedIndex = -1;
    private String currentWorldKey;

    // 🔒 引入一个重置锁标记，用来在批量清空 UI、刷新列表时，拦截并屏蔽不必要触发的选择事件
    private boolean isResettingUi = false;

    public TimerPresenter(TimerListView listView, TimerBaseConfigView baseConfigView,
                          TimerPolicyConfigView policyConfigView, ActionConfigView actionConfigView,
                          TimerMonitorView monitorView) {
        this.listView = listView;
        this.baseConfigView = baseConfigView;
        this.policyConfigView = policyConfigView;
        this.actionConfigView = actionConfigView;
        this.monitorView = monitorView;

        initEventBindings();
    }

    /**
     * 绑定原子视图之间的交互事件
     */
    private void initEventBindings() {
        // 1. 左侧列表行选中切换
        this.listView.addSelectionListener(index -> {
            // 如果当前正在进行删除等清空 UI 的底层重置动作，不处理任何中间态的选择事件，防止回填脏数据
            if (isResettingUi) {
                return;
            }

            // 在切换前，先尝试暂存当前正在编辑的行数据到内存列表中
            saveUiToMemory();
            
            this.currentSelectedIndex = index;
            if (index >= 0 && index < currentTimerList.size()) {
                TimerConfigEntry selectedTimer = currentTimerList.get(index);
                // 拆流回填到各个原子配置 View 中
                baseConfigView.setBaseData(selectedTimer);
                policyConfigView.setPolicyData(selectedTimer);
                actionConfigView.setActionData(selectedTimer.getAction());
            } else {
                // 清空右侧输入
                clearRightConfigUi();
            }
        });

        // 2. 左侧“添加”按钮动作
        this.listView.setOnAddAction(() -> {
            TimerConfigEntry newTimer = new TimerConfigEntry();
            newTimer.setId("timer_" + UUID.randomUUID().toString().substring(0, 8));
            newTimer.setName("未命名定时器");
            newTimer.setEnable(true);
            newTimer.setType("delay");
            newTimer.setTriggerValue("1000"); // 默认 1 秒延迟
            newTimer.getAction().setType("SendCommand");

            currentTimerList.add(newTimer);
            
            // 局部刷新，此时直接 select 新增项
            listView.refreshList(currentTimerList);
            monitorView.refreshMonitorData(currentTimerList); 
            listView.select(currentTimerList.size() - 1); 
        });

        // 3. 左侧“删除”按钮动作 (按照您的宏观思路彻底重构)
        this.listView.setOnDeleteAction(() -> {
            int index = listView.getSelectedIndex();
            if (index >= 0 && index < currentTimerList.size()) {
                
                // 🚀 进入重置状态流
                isResettingUi = true;
                try {
                    // 【步骤 A】：切断状态并直接清空 UI 视图（防脏数据死灰复燃的根本）
                    this.currentSelectedIndex = -1;
                    this.listView.clearSelection(); 
                    clearRightConfigUi();

                    // 【步骤 B】：处理核心数据移除与基础同步
                    currentTimerList.remove(index);
                    listView.refreshList(currentTimerList);
                    monitorView.refreshMonitorData(currentTimerList);
                    
                } finally {
                    // 🚀 核心 UI 操作结束，解锁事件响应
                    isResettingUi = false;
                }

                // 【步骤 C】：数据与底盘干净后，再做干净的 select 重新占位
                if (!currentTimerList.isEmpty()) {
                    // 智能算法：若删除了最后一条则选倒数第一条；否则索引保持原地不动
                    int nextSelectIndex = Math.min(index, currentTimerList.size() - 1);
                    listView.select(nextSelectIndex);
                }
            }
        });

        // 4. 底部监控表格修改“启用”状态开关的快捷联动
        this.monitorView.setOnStatusChangedListener((row, isEnabled) -> {
            if (row >= 0 && row < currentTimerList.size()) {
                TimerConfigEntry timer = currentTimerList.get(row);
                timer.setEnable(isEnabled); // 同步内存数据
                
                // 如果当前正在编辑的就是被勾选的这一行，同时同步右上角的配置视图
                if (row == currentSelectedIndex) {
                    baseConfigView.setBaseData(timer);
                }
            }
        });

        // 5. 底部监控表格“右键立即触发一次”按钮联动
        this.monitorView.setOnTriggerImmediatelyListener(row -> {
            if (row >= 0 && row < currentTimerList.size()) {
                TimerConfigEntry timer = currentTimerList.get(row);
                JOptionPane.showMessageDialog(null, "测试触发：立即发送指令 -> " + timer.getAction().getExpression());
            }
        });
    }

    /**
     * 当顶栏的 MUD 下拉框发生切换时，由主窗口调用它加载对应世界（北侠）的定时器 JSON
     */
    public void loadWorldTimers(String worldKey) {
        saveCurrentTimer();

        this.currentWorldKey = worldKey;
        this.currentTimerList = TimerService.getTimersByWorld(worldKey);
        if (this.currentTimerList == null) {
            this.currentTimerList = new ArrayList<>();
        }

        isResettingUi = true;
        try {
            this.listView.refreshList(currentTimerList);
            this.monitorView.refreshMonitorData(currentTimerList);
            clearRightConfigUi();
            this.currentSelectedIndex = -1;
        } finally {
            isResettingUi = false;
        }

        if (!currentTimerList.isEmpty()) {
            this.listView.select(0);
        } else {
            this.listView.clearSelection();
        }
    }

    /**
     * 独立封装的原子方法：一键清空右侧所有细节配置 UI 区域
     */
    private void clearRightConfigUi() {
        baseConfigView.setBaseData(null);
        policyConfigView.setPolicyData(null);
        actionConfigView.setActionData(null);
    }

    /**
     * 将当前右侧各个原子 UI 输入框的值暂存写入对应的内存对象中
     */
    private void saveUiToMemory() {
        // 如果索引无效或正在重置UI，强行中断，不处理内存重写
        if (isResettingUi || currentSelectedIndex < 0 || currentSelectedIndex >= currentTimerList.size()) {
            return;
        }
        
        TimerConfigEntry timer = currentTimerList.get(currentSelectedIndex);
        baseConfigView.getBaseData(timer);
        policyConfigView.getPolicyData(timer);
        timer.setAction(actionConfigView.getActionData()); 
        
        // 💡 警告：由于这两句会强行刷洗 UI 并引发事件连锁，在重构后已被保护。
        listView.refreshList(currentTimerList);
        monitorView.refreshMonitorData(currentTimerList);
    }

    /**
     * 点击主窗口“确定”或者退出时引发的持久化保存动作
     */
    public void saveCurrentTimer() {
        saveUiToMemory(); 
        if (currentWorldKey != null) {
            TimerService.saveTimersToWorld(currentWorldKey, currentTimerList);
        }
    }
}

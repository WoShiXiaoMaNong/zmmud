package zm.mud.ui.component.menu.setting.trigger;

import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.utils.SpringBeanUtil;
import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class TriggerPresenter {
    private final TriggerListView listView;
    private final BaseConfigView baseView;
    private final MatcherConfigView matcherView;
    private final ActionConfigView actionView;

    // 当前正在编辑的游戏世界代码，默认是北侠
    private String currentMudWorld = ""; 
    // 当前正在内存中管理的全部触发器数据缓存
    private List<TriggerConfigEntry> configEntries;

    public TriggerPresenter(TriggerListView listView, BaseConfigView baseView, 
                            MatcherConfigView matcherView, ActionConfigView actionView) {
        this.listView = listView;
        this.baseView = baseView;
        this.matcherView = matcherView;
        this.actionView = actionView;

        initEventBinding();
        loadWorldTriggers(currentMudWorld);
    }

    /**
     * 根据游戏世界加载全部触发器到左侧列表
     */
    public void loadWorldTriggers(String worldCode) {
        this.currentMudWorld = worldCode;
        // 核心：直接调用你刚刚写好的底层接入接口
        List<TriggerConfigEntry> entries = TriggerService.getTriggerConfigEntries(worldCode);
        
        // 🛠️ 健壮性优化：防止底层返回的 List 是不可变列表（如 Arrays.asList），将其转为可变列表方便增删
        this.configEntries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
        
        // 更新左侧列表 UI
        listView.refreshList(this.configEntries);
        
        // 默认选中第一行（如果有数据的话）
        if (!configEntries.isEmpty()) {
            // 这里会自动触发 SelectionListener 回显数据
            // 若 listView 内部未实现此方法，可通过底层 listComponent 或手动回显第一条
        } else {
            clearAllViews();
        }
    }

    private void initEventBinding() {
        // 当用户点击左侧触发器列表某一项时
        listView.addSelectionListener(selectedIndex -> {
            if (selectedIndex < 0 || selectedIndex >= configEntries.size()) {
                clearAllViews();
                return;
            }
            
            // 拿到当前选中的配置条目
            TriggerConfigEntry entry = configEntries.get(selectedIndex);
            
            // ─── 数据回显 ───
            baseView.setBaseData(entry);
            matcherView.setMatcherData(entry.getMatcher());
            actionView.setActionData(entry.getAction());
        });

        // ========================================================
        // 绑定“添加”按钮点击事件
        // ========================================================
        listView.setOnAddAction(() -> {
            // 1. 创建全新的空触发器对象并初始化内部结构，防止回显时报空指针
            TriggerConfigEntry newEntry = new TriggerConfigEntry();
            newEntry.setName(""); // 留空，列表刷新时会显示你定义的 "[未命名触发器]"
            newEntry.setMatcher(new MatcherAndActionConfigEntry());
            newEntry.setAction(new MatcherAndActionConfigEntry());
            
            // 2. 追加到内存缓存中
            configEntries.add(newEntry);
            
            // 3. 刷新左侧列表 UI 展示
            listView.refreshList(configEntries);
            
            // 4. 数据联动：右侧界面立即切入新触发器的编辑状态
            baseView.setBaseData(newEntry);
            matcherView.setMatcherData(newEntry.getMatcher());
            actionView.setActionData(newEntry.getAction());
            
            // 5. 让左侧列表强行高亮选中刚刚新建的最后一行
            // 注意：因为刷新后触发了监听器，所以它会自动走一遍上面的数据回显逻辑，确保状态完全同步
            int lastIndex = configEntries.size() - 1;
            
             listView.select(lastIndex); 
        });

        // ========================================================
        // 绑定“删除”按钮点击事件
        // ========================================================
        listView.setOnDeleteAction(() -> {
            int selectedIndex = listView.getSelectedIndex();
            
            // 1. 安全校验：检查玩家当前是否选中了有效的行
            if (selectedIndex < 0 || selectedIndex >= configEntries.size()) {
                JOptionPane.showMessageDialog(null, "请先在列表中选择要删除的触发器", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 2. 友好提示：防止玩家误触导致写好的触发器丢失
            TriggerConfigEntry target = configEntries.get(selectedIndex);
            String displayName = (target.getName() == null || target.getName().isEmpty()) ? "未命名触发器" : target.getName();
            
            int confirm = JOptionPane.showConfirmDialog(
                null, 
                "确定要删除触发器 [" + displayName + "] 吗？\n删除后不可恢复。", 
                "确认删除", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            // 3. 执行删除逻辑
            if (confirm == JOptionPane.YES_OPTION) {
                // 从内存缓存中移除该条目
                configEntries.remove(selectedIndex);
                
                // 刷新左侧 UI 列表
                listView.refreshList(configEntries);
                
                // 4. 清除或重置右侧表单焦点
                if (configEntries.isEmpty()) {
                    // 如果删光了，彻底清空右侧表单
                    listView.clearSelection();
                    clearAllViews();
                } else {
                    // 如果还有剩余触发器，智能地将焦点向前移一位或停留在当前索引边界内
                    int nextSelectIndex = Math.min(selectedIndex, configEntries.size() - 1);
                 
                    listView.select(nextSelectIndex);
                    
                    // 如果暂无直接选中行的方法，先手动刷新右侧为新一行的内容：
                    TriggerConfigEntry nextEntry = configEntries.get(nextSelectIndex);
                    baseView.setBaseData(nextEntry);
                    matcherView.setMatcherData(nextEntry.getMatcher());
                    actionView.setActionData(nextEntry.getAction());
                }
            }
        });
    }

    /**
     * 当点击“确定”或本地保存按钮时
     */
    public void saveCurrentTrigger() {
        // 检查是修改已有触发器，还是点击了新建
        int selectedIndex = listView.getSelectedIndex();
        TriggerConfigEntry entry;
        
        if (selectedIndex >= 0 && selectedIndex < configEntries.size()) {
            entry = configEntries.get(selectedIndex); // 修改已有的
        } else {
            entry = new TriggerConfigEntry(); // 创建新的
            configEntries.add(entry);
        }

        // ─── 收集三个小组件面板上的数据 ───
        baseView.getBaseData(entry);
        entry.setMatcher(matcherView.getMatcherData());
        entry.setAction(actionView.getActionData());

        // 校验
        if (entry.getName() == null || entry.getName().isEmpty()) {
            JOptionPane.showMessageDialog(null, "触发器名称不能为空", "校验失败", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        TriggerFactory factory = SpringBeanUtil.getBean(
                TriggerFactory.class);
         factory.save(currentMudWorld, configEntries);

        // 刷新左侧面板显示
        listView.refreshList(configEntries);
    }

    private void clearAllViews() {
        baseView.setBaseData(null);
        matcherView.setMatcherData(null);
        actionView.setActionData(null);
    }

    public String getCurrentMudWorld() {
        return currentMudWorld;
    }

    public List<TriggerConfigEntry> getConfigEntries() {
        return configEntries;
    }

    
}

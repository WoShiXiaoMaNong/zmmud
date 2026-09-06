package zm.mud.ui.component.menu.setting.trigger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;

public class TriggerListView extends JPanel {
    private JList<String> listComponent;
    private DefaultListModel<String> listModel;
    private JButton addButton;
    private JButton deleteButton;

    public TriggerListView() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(200, 0)); // 设定左侧合理的固定宽度
        setBorder(BorderFactory.createTitledBorder("触发器列表"));
        initViews();
    }
private void initViews() {
    // 1. 初始化列表模型
    listModel = new DefaultListModel<>();
    listComponent = new JList<>(listModel);
    listComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // ==========================================
    // ⭐ 核心优化：通过自定义渲染器让行与行更紧凑
    // ==========================================
    listComponent.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
            // 调用父类方法获取默认的渲染组件（其实就是一个 JLabel）
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            // 调整内边距：上下设为 2 像素（默认通常较大），左右设为 5 像素
            label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            
            return label;
        }
    });

    // 包裹进支持颜色和区域滚动的面板
    JScrollPane scrollPane = new JScrollPane(listComponent);
    add(scrollPane, BorderLayout.CENTER);

    // 2. 底部控制按钮 (添加 / 删除)
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
    addButton = new JButton("添加");
    deleteButton = new JButton("删除");
    buttonPanel.add(addButton);
    buttonPanel.add(deleteButton);
    add(buttonPanel, BorderLayout.SOUTH);
}


    /**
     * 【重要】刷新左侧数据源，Presenter 在获取到数据列表后调用它同步
     */
    public void refreshList(List<TriggerConfigEntry> entries) {
        // 缓存当前选中的下标位置，防止刷新后丢失焦点
        int selectedIndex = listComponent.getSelectedIndex();
        
        listModel.clear();
        if (entries != null) {
            for (TriggerConfigEntry entry : entries) {
                // 如果在编辑中名字为空，展示占位符
                listModel.addElement(entry.getName() == null || entry.getName().isEmpty() ? "[未命名触发器]" : entry.getName());
            }
        }

        // 恢复焦点
        if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
            listComponent.setSelectedIndex(selectedIndex);
        }
    }

    /**
     * 提供给外部 Presenter 的回调接口：用于注册左侧行选切换监听
     */
    public void addSelectionListener(Consumer<Integer> listener) {
        listComponent.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // 防止点击鼠标时连续响应两次
                listener.accept(listComponent.getSelectedIndex());
            }
        });
    }

    /**
     * 暴露按钮点击事件给 Presenter 绑定动作
     */
    public void setOnAddAction(Runnable action) {
        addButton.addActionListener(e -> action.run());
    }

    public void setOnDeleteAction(Runnable action) {
        deleteButton.addActionListener(e -> action.run());
    }

    public int getSelectedIndex() {
        return listComponent.getSelectedIndex();
    }

    public void select(int index){
        this.listComponent.setSelectedIndex(index);
    }

    public void clearSelection() {
        listComponent.clearSelection();
    }
}

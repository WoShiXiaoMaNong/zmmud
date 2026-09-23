package zm.mud.ui.component.menu.setting.timer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import zm.mud.core.automation.timer.cfg.TimerConfigEntry;

/**
 * 定时器左侧列表组件（对齐 Trigger 列表体系规范）
 */
public class TimerListView extends JPanel {
    private JList<String> listComponent;
    private DefaultListModel<String> listModel;
    private JButton addButton;
    private JButton deleteButton;
    
    // 💡 核心修复：引入 UI 刷新状态锁，防止代码更新 Model 时触发业务监听器导致无限递归
    private boolean isUpdatingUi = false;

    public TimerListView() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(200, 0)); // 严格对齐左侧固定宽度
        setBorder(BorderFactory.createTitledBorder("定时器列表"));
        initViews();
    }

    private void initViews() {
        listModel = new DefaultListModel<>();
        listComponent = new JList<>(listModel);
        listComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        listComponent.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // 紧凑型内边距
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listComponent);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        addButton = new JButton("添加");
        deleteButton = new JButton("删除");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshList(List<TimerConfigEntry> entries) {
        // 💡 核心修复：开启状态锁
        isUpdatingUi = true;
        try {
            int selectedIndex = listComponent.getSelectedIndex();
            listModel.clear();
            if (entries != null) {
                for (TimerConfigEntry entry : entries) {
                    listModel.addElement(
                            entry.getName() == null || entry.getName().isEmpty() ? "[未命名定时器]" : entry.getName());
                }
            }
            if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
                listComponent.setSelectedIndex(selectedIndex);
            }
        } finally {
            // 💡 核心修复：无论刷新是否成功，最终必须解开锁，让用户点击能正常响应
            isUpdatingUi = false;
        }
    }

    public void addSelectionListener(Consumer<Integer> listener) {
        listComponent.addListSelectionListener(e -> {
            // 💡 核心修复：如果当前是在通过代码 refreshList 更新 UI，直接拦截，绝不往下透传事件
            if (isUpdatingUi) {
                return;
            }
            if (!e.getValueIsAdjusting()) {
                listener.accept(listComponent.getSelectedIndex());
            }
        });
    }

    public void setOnAddAction(Runnable action) { addButton.addActionListener(e -> action.run()); }
    public void setOnDeleteAction(Runnable action) { deleteButton.addActionListener(e -> action.run()); }
    public int getSelectedIndex() { return listComponent.getSelectedIndex(); }
    public void select(int index) { this.listComponent.setSelectedIndex(index); }
    public void clearSelection() { listComponent.clearSelection(); }
}

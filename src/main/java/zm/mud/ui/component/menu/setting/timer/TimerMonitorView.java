package zm.mud.ui.component.menu.setting.timer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import zm.mud.core.automation.timer.cfg.TimerConfigEntry; // 假设对应运行时或配置实体

/**
 * 定时器监控器（仪表盘组件）
 */
public class TimerMonitorView extends JPanel {
    private JTable monitorTable;
    private DefaultTableModel tableModel;
    private JPopupMenu popupMenu;
    private java.util.function.BiConsumer<Integer, Boolean> onStatusChangedListener; // 状态开关回调
    private java.util.function.Consumer<Integer> onTriggerImmediatelyListener;      // 立即触发回调

    public TimerMonitorView() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("定时器监控器 (运行时状态)"));
        initViews();
        setupPopupMenu();
    }

    private void initViews() {
        // 定义表格列名（严格遵照你的构想：是否启用、名称、启动时间、下一次触发时间、运行时长/倒计时）
        String[] columnNames = {"启用", "定时器名称", "类型", "启动时间/延迟", "下一次触发", "运行倒计时/计数"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // 第 0 列“状态”渲染为 CheckBox
                if (columnIndex == 0) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有第 0 列的开关在表格中允许玩家直接勾选切换
                return column == 0;
            }
        };

        monitorTable = new JTable(tableModel);
        monitorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        monitorTable.getTableHeader().setReorderingAllowed(false); // 禁止拖动列

        // 限制第一列 CheckBox 的最优宽度
        monitorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        monitorTable.getColumnModel().getColumn(0).setMaxWidth(60);

        // 监听表格单元格数据修改（主要捕获第0列状态切换）
        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 0) {
                int row = e.getFirstRow();
                Boolean isEnabled = (Boolean) tableModel.getValueAt(row, 0);
                if (onStatusChangedListener != null) {
                    onStatusChangedListener.accept(row, isEnabled);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(monitorTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 设置右键弹出快捷菜单 (对挂机玩家极其友好)
     */
    private void setupPopupMenu() {
        popupMenu = new JPopupMenu();
        JMenuItem triggerItem = new JMenuItem("立即触发一次");
        triggerItem.addActionListener(e -> {
            int row = monitorTable.getSelectedRow();
            if (row >= 0 && onTriggerImmediatelyListener != null) {
                onTriggerImmediatelyListener.accept(row);
            }
        });
        popupMenu.add(triggerItem);

        monitorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handlePopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handlePopup(e); }

            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = monitorTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        monitorTable.setRowSelectionInterval(row, row);
                        popupMenu.show(monitorTable, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    /**
     * 核心刷新方法：由 Presenter 绑定心跳线程，定时（例如每秒）调用此方法刷新运行时表格
     */
     /**
     * 【修正后】核心刷新方法：严格适配官方定义的 TimerConfigEntry 结构
     */
    public void refreshMonitorData(List<TimerConfigEntry> runtimes) {
        // 先记录当前被选中的行，防止连续刷新导致玩家丢失行选中焦点
        int selectedRow = monitorTable.getSelectedRow();

        tableModel.setRowCount(0); // 清空数据
        if (runtimes == null) return;

        for (TimerConfigEntry t : runtimes) {
            // 获取运行时参数 Map
            java.util.Map<String, Object> runtimeMap = t.getRuntimeParams();
            String nextTrigger = "--";
            String durationOrCountdown = "--";
            
            if (runtimeMap != null) {
                nextTrigger = runtimeMap.getOrDefault("nextTriggerTime", "--").toString();
                durationOrCountdown = runtimeMap.getOrDefault("runningDuration", "--").toString();
            }

            // 组装运行时状态行
            Object[] rowData = new Object[] {
                t.isEnable(),                                                    // 状态 (原生 boolean -> 自动呈现为 CheckBox)
                t.getName() == null || t.getName().isEmpty() ? "[未命名]" : t.getName(), // 名称
                "delay".equalsIgnoreCase(t.getType()) ? "延时" : "定点",          // 类型 (忽略大小写兼容)
                t.getTriggerValue() == null ? "--" : t.getTriggerValue(),       // 配置参数：延迟毫秒数或 Cron 表达式均存在该字段
                nextTrigger,                                                     // 下一次触发时间
                durationOrCountdown                                              // 运行倒计时或运行时长描述
            };
            tableModel.addRow(rowData);
        }

        // 恢复选中状态
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            monitorTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

 
    // 暴露监听器给 Presenter 进行业务逻辑绑定
    public void setOnStatusChangedListener(BiConsumer<Integer, Boolean> listener) { this.onStatusChangedListener = listener; }
    public void setOnTriggerImmediatelyListener(Consumer<Integer> listener) { this.onTriggerImmediatelyListener = listener; }
}

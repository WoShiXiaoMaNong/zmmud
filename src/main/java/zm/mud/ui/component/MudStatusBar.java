package zm.mud.ui.component;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import java.awt.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;

public class MudStatusBar extends JPanel {
    // 1. 定义状态栏第一行标签集合，包含属性名称、颜色和对应的状态数据键
    private static final List<StatusBarLabel> statusBarLabels = new ArrayList<>();
    static {
        statusBarLabels.add(new StatusBarLabel("姓名", Color.LIGHT_GRAY, List.of("name", "id")));
        statusBarLabels.add(new StatusBarLabel("气血", Color.GREEN, List.of("qi", "max_qi")));
        statusBarLabels.add(new StatusBarLabel("精气", Color.LIGHT_GRAY, List.of("jing", "max_jing")));
        statusBarLabels.add(new StatusBarLabel("内力", Color.LIGHT_GRAY, List.of("neili", "max_neili")));
        statusBarLabels.add(new StatusBarLabel("饱食/饮水", Color.LIGHT_GRAY, List.of("food", "water")));
        statusBarLabels.add(new StatusBarLabel("等级/经验", Color.LIGHT_GRAY, List.of("level", "combat_exp")));

        
    };

    private Color backgroundColor = new Color(40, 44, 52); // 状态栏背景色
    private MatteBorder border = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY); // 状态栏边框
    private Font font = new Font("Monospaced", Font.BOLD, 13); // 状态栏字体

    // 第二行：房间位置标签
    private JLabel lblRoom = new JLabel("位置: -- [出口: --]");

    public MudStatusBar() {
        // 1. 设置整体状态栏背景与边框
        setBackground(backgroundColor);
        setBorder(border);

        // 2. 主布局采用【垂直群组/网格布局】，比 BoxLayout 更能强力确保多行百分百显示
        // 2行，1列，行间距 0
        setLayout(new GridLayout(2, 1, 0, 0));

        // ================== 【第一行：创建并组装属性面板】 ==================
        // 使用 FlowLayout，上下垂直间距设为 2 像素，防止撑开
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        row1.setOpaque(false);
        for (StatusBarLabel sbl : statusBarLabels) {
            JLabel label = sbl.getLabel();
            row1.add(label);
        }

        // ================== 【第二行：创建并组装位置面板】 ==================
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        row2.setOpaque(false);

        lblRoom.setFont(font);
        lblRoom.setForeground(new Color(102, 217, 239)); // 亮青色
        row2.add(lblRoom);

        // 3. 将两行面板纵向垂直塞入主状态栏（GridLayout 会强制两行平分 55px 高度）
        add(row1);
        add(row2);
        this.refreshStatus(null, null);
    }

    /**
     * 刷新方法
     */
    public void refreshStatus(Map<String, Object> statusData, PkuxkxRoom room) {
        // 1. 刷新第一行人物属性
        for (StatusBarLabel sbl : statusBarLabels) {
            JLabel label = sbl.getLabel();
            List<String> values = new ArrayList<>();
            for (String key : sbl.getKeys()) {
                if (statusData != null && statusData.containsKey(key)) {
                    String value = String.valueOf(statusData.get(key));
                    if( value == null ){
                        value = "--";
                    }
                    values.add(value);
                } else {
                    values.add("--");
                }
            }
            if (!values.isEmpty()) {
                label.setText(sbl.getText() + ": " + String.join("/", values));
            }
        }

        // 2. 刷新第二行房间位置
        if (room != null && room.getResult() && room.getName() != null) {
            String roomName = room.getName();
            List<String> exitsList = room.getDir();
            String exits = (exitsList == null || exitsList.isEmpty()) ? "无" : String.join(", ", exitsList);
            lblRoom.setText(String.format("当前位置: 📍 %s  [ 出口: %s ]", roomName, exits));
        } else {
            lblRoom.setText("当前位置: 📍 探索中...  [ 出口: -- ]");
        }

        // 强制刷新渲染
        revalidate();
        repaint();
    }

    private static class StatusBarLabel {
        private String text;
        private Color color;
        private List<String> keys;
        private JLabel label;

        public StatusBarLabel(String text, Color color, List<String> keys) {
            this.text = text;
            this.color = color;
            this.keys = keys;
            this.label = new JLabel(text);
            this.label.setForeground(color);
            this.label.setFont(new Font("Monospaced", Font.BOLD, 13));
        }

        public String getText() {
            return text;
        }

        public Color getColor() {
            return color;
        }

        public List<String> getKeys() {
            return keys;
        }

        public JLabel getLabel() {
            return label;
        }

    }
}
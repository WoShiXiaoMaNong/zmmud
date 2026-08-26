package zm.mud.ui.component;


import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.List;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;

public class MudStatusBar extends JPanel {

    // 第一行：人物属性标签
    private JLabel lblName = new JLabel("姓名: --");
    private JLabel lblQi = new JLabel("气血: --/--");
    private JLabel lblJing = new JLabel("精气: --/--");
    private JLabel lblNeili = new JLabel("内力: --/--");
    private JLabel lblFoodWater = new JLabel("食物/饮水: --/--");
    
    // 第二行：房间位置标签
    private JLabel lblRoom = new JLabel("位置: -- [出口: --]");

       public MudStatusBar() {
        // 1. 设置整体状态栏背景与边框
        setBackground(new Color(40, 44, 52)); 
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY)); 
        
        // 2. 主布局采用【垂直群组/网格布局】，比 BoxLayout 更能强力确保多行百分百显示
        // 2行，1列，行间距 0
        setLayout(new GridLayout(2, 1, 0, 0)); 

        // 公用字体样式
        Font font = new Font("Monospaced", Font.BOLD, 13);

        // ================== 【第一行：创建并组装属性面板】 ==================
        // 使用 FlowLayout，上下垂直间距设为 2 像素，防止撑开
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        row1.setOpaque(false); 
        
        Component[] vVitals = {lblName, lblQi, lblJing, lblNeili, lblFoodWater};
        for (Component c : vVitals) {
            c.setFont(font);
            c.setForeground(Color.LIGHT_GRAY);
            row1.add(c);
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
    }


    /**
     * 刷新方法
     */
    public void refreshStatus(Map<String, Object> statusData, PkuxkxRoom room) {
        // 1. 刷新第一行人物属性
        if (statusData != null && !statusData.isEmpty()) {
            String name = String.valueOf(statusData.getOrDefault("name", "武源"));
            String id = String.valueOf(statusData.getOrDefault("id", ""));
            lblName.setText(String.format("【%s(%s)】", name, id));

            String qi = String.valueOf(statusData.getOrDefault("qi", "0"));
            String maxQi = String.valueOf(statusData.getOrDefault("max_qi", "0"));
            lblQi.setText(String.format("气血: %s/%s", qi, maxQi));
            if (Integer.parseInt(qi) < Integer.parseInt(maxQi) * 0.3) {
                lblQi.setForeground(Color.RED); 
            } else {
                lblQi.setForeground(Color.GREEN);
            }

            String jing = String.valueOf(statusData.getOrDefault("jing", "0"));
            String maxJing = String.valueOf(statusData.getOrDefault("max_jing", "0"));
            lblJing.setText(String.format("精气: %s/%s", jing, maxJing));

            String neili = String.valueOf(statusData.getOrDefault("neili", "0"));
            String maxNeili = String.valueOf(statusData.getOrDefault("max_neili", "0"));
            lblNeili.setText(String.format("内力: %s/%s", neili, maxNeili));

            String food = String.valueOf(statusData.getOrDefault("food", "0"));
            String water = String.valueOf(statusData.getOrDefault("water", "0"));
            lblFoodWater.setText(String.format("饱食/饮水: %s/%s", food, water));
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
}
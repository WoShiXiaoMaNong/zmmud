package zm.mud.ui.component.statusBar;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import com.alibaba.fastjson2.TypeReference;

import java.awt.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;
import zm.mud.ui.UiConfigLoader;

@SuppressWarnings("unchecked")
public class MudStatusBar extends JPanel {
    
    // 状态栏属性标签的多行集合（可随时在里面增删行、增删列元素）
    private static final List<List<StatusBarLabel>> statusBarLabels = new ArrayList<>();
    static {
        List<List<StatusBarLabelInfo>> config = (List<List<StatusBarLabelInfo>>) UiConfigLoader.loadUIConfig("pkuxkx", "pkuxkx.status_bar",new TypeReference<List<List<StatusBarLabelInfo>>>(){});
        
        for(List<StatusBarLabelInfo> infos : config ){
            List<StatusBarLabel> row = new ArrayList<>();
            for( StatusBarLabelInfo info : infos){
                row.add(new StatusBarLabel(info.getPrefix(), Color.decode(info.getColor()), info.getKeys(), info.getFixedWidth()));
            }
            statusBarLabels.add(row);
        }
    
    }

    private final Color backgroundColor = new Color(30, 34, 42); 
    private final MatteBorder border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY); 

    // 房间位置标签
    private final JLabel lblRoom = new JLabel("当前位置: 📍 探索中... [ 出口: -- ]");

    public MudStatusBar() {
        setBackground(backgroundColor);
        setBorder(border);

        // 统一使用一个全局 GridBagLayout
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 12, 1, 12); 

        // 1. 【动态计算】算出当前配置中，哪一行的列数最多
        int maxColumns = 1;
        for (List<StatusBarLabel> row : statusBarLabels) {
            if (row.size() > maxColumns) {
                maxColumns = row.size();
            }
        }

        // 2. 【网格化组装属性】
        int currentRow = 0;
        for (int row = 0; row < statusBarLabels.size(); row++) {
            List<StatusBarLabel> rowLabels = statusBarLabels.get(row);
            int rowSize = rowLabels.size();
            
            for (int col = 0; col < rowSize; col++) {
                StatusBarLabel sbl = rowLabels.get(col);
                
                gbc.gridx = col;        // 当前列坐标
                gbc.gridy = currentRow; // 当前行坐标
                
                // 如果是当前行的最后一个元素
                if (col == rowSize - 1) {
                    // 动态计算剩余需要横跨的列数，确保每一行的末尾都能顶到最右侧网格边界
                    gbc.gridwidth = maxColumns - rowSize + 1; 
                    gbc.weighty = 0.0; // 属性行高度保持紧凑
                    gbc.weightx = 1.0; // 最后一列吞掉右侧剩余的所有空白空间
                } else {
                    gbc.gridwidth = 1;
                    gbc.weighty = 0.0; // 属性行高度保持紧凑
                    gbc.weightx = 0.0; // 普通列保持紧凑
                }
                
                add(sbl.getLabel(), gbc);
            }
            currentRow++;
        }

        // 3. 【网格化组装位置面板】
        gbc.gridx = 0;
        gbc.gridy = currentRow;      // 动态紧跟在属性行下方
        gbc.gridwidth = maxColumns;  // 动态横跨整个网格的最大列数
        gbc.weightx = 1.0;
        
        lblRoom.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lblRoom.setForeground(new Color(102, 217, 239)); 
        add(lblRoom, gbc);

        // 4. 【动态计算整体高度】总行数 = 属性List的Size + 1行地图
        int totalRows = statusBarLabels.size() + 1;
        this.setPreferredSize(new Dimension(800, 20 * totalRows));
        
        this.refreshStatus(null, null);
    }

    /**
     * 刷新方法
     */
    public void refreshStatus(Map<String, Object> statusData, PkuxkxRoom room) {
        // 1. 动态刷新属性标签文本
        for (List<StatusBarLabel> rowLabels : statusBarLabels) {
            for (StatusBarLabel sbl : rowLabels) {
                List<String> values = new ArrayList<>();
                for (String key : sbl.getKeys()) {
                    if (statusData != null && statusData.containsKey(key)) {
                        String value = String.valueOf(statusData.get(key));
                        values.add((value == null || "null".equals(value)) ? "--" : value);
                    } else {
                        values.add("--");
                    }
                }
                
                String valueStr = String.join("/", values);
                String fullText = sbl.getPrefix() + ": " + valueStr;
                
                if (fullText.length() < sbl.getFixedWidth()) {
                    fullText = String.format("%-" + sbl.getFixedWidth() + "s", fullText);
                }
                
                sbl.getLabel().setText(fullText);
            }
        }

        // 2. 刷新房间位置文本
        if (room != null && room.getResult() && room.getName() != null) {
            String roomName = room.getName();
            List<String> exitsList = room.getDir();
            String exits = (exitsList == null || exitsList.isEmpty()) ? "无" : String.join(", ", exitsList);
            lblRoom.setText(String.format("当前位置: 📍 %s  [ 出口: %s ]", roomName, exits));
        } else {
            lblRoom.setText("当前位置: 📍 探索中...  [ 出口: -- ]");
        }

        revalidate();
        repaint();
    }

    /**
     * 封装的内部标签类
     */
    private static class StatusBarLabel {
        private final String prefix;
        private final List<String> keys;
        private final JLabel label;
        private final int fixedWidth;

        public StatusBarLabel(String prefix, Color color, List<String> keys, int fixedWidth) {
            this.prefix = prefix;
            this.keys = keys;
            this.fixedWidth = fixedWidth;
            this.label = new JLabel(prefix + ": --");
            this.label.setForeground(color);
            this.label.setFont(new Font("Monospaced", Font.PLAIN, 13)); 
        }

        public String getPrefix() { return prefix; }
        public List<String> getKeys() { return keys; }
        public JLabel getLabel() { return label; }
        public int getFixedWidth() { return fixedWidth; }
    }
}

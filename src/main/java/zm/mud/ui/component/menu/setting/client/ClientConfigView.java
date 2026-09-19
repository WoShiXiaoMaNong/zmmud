package zm.mud.ui.component.menu.setting.client;


import javax.swing.*;
import java.awt.*;
import java.util.List;
import zm.mud.utils.FontUtil;

/**
 * 客户端具体属性配置面板（使用等宽字体列表）
 */
public class ClientConfigView extends JPanel {

    private JComboBox<Font> fontCombo;        // 字体下拉框（直接存储Font对象）
    private JComboBox<Integer> fontSizeCombo; // 字体大小下拉框

    public ClientConfigView() {
        this.setBorder(BorderFactory.createTitledBorder("界面显示配置"));
        this.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 1. 字体选择行
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        this.add(new JLabel("客户端字体 (等宽):"), gbc);

        // 🛠️ 核心修改：通过 FontUtil 过滤并获取系统及注册的所有等宽字体
        List<Font> monospacedFonts = FontUtil.getAllMonospacedFonts();
        Font[] fontArray = monospacedFonts.toArray(new Font[0]);
        
        fontCombo = new JComboBox<>(fontArray);
        // 🛠️ 自定义渲染器：让下拉框只显示清晰的字体家族名称（如 "Sarasa Mono SC" 或 "Courier New"）
        fontCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Font) {
                    setText(((Font) value).getFamily());
                }
                return this;
            }
        });
        fontCombo.setPreferredSize(new Dimension(180, fontCombo.getPreferredSize().height));
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(fontCombo, gbc);

        // 2. 字号选择行
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(new JLabel("字体大小 (pt):"), gbc);

        // 预设常用的 MUD 字体大小
        Integer[] fontSizes = {12, 14, 16, 18, 20, 22, 24, 26, 28, 32};
        fontSizeCombo = new JComboBox<>(fontSizes);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(fontSizeCombo, gbc);
        
        // 3. 垫片
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        this.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, Short.MAX_VALUE)), gbc);
    }

    /**
     * 由外部传入数据进行视图赋值
     * @param fontName 字体家族名称（如 "Courier New"）
     * @param fontSize 字体大小
     */
    public void setConfig(String fontName, int fontSize) {
        // 根据传入的字体名称，遍历下拉框找到匹配的 Font 项
        if (fontName != null) {
            for (int i = 0; i < fontCombo.getItemCount(); i++) {
                Font font = fontCombo.getItemAt(i);
                if (fontName.equalsIgnoreCase(font.getFamily()) || fontName.equalsIgnoreCase(font.getName())) {
                    fontCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        fontSizeCombo.setSelectedItem(fontSize);
    }

    /**
     * 获取当前选中的字体名称（推荐返回 Family 名称，以保证跨平台/变体的兼容性）
     */
    public String getSelectedFontName() {
        Font selectedFont = (Font) fontCombo.getSelectedItem();
        return selectedFont != null ? selectedFont.getFamily() : "Monospaced";
    }

    /**
     * 获取当前选中的字体大小
     */
    public int getSelectedFontSize() {
        Object selected = fontSizeCombo.getSelectedItem();
        return selected != null ? (Integer) selected : 16;
    }
}
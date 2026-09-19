package zm.mud.ui.component.statusBar;

import java.util.List;

public class StatusBarLabelInfo {
       /**
     * 前缀名称 (例如: "ID", "姓名", "饱食/饮水")
     */
    private String prefix;

    /**
     * 颜色十六进制码 (例如: "#F92672")
     */
    private String color;

    /**
     * 绑定的键值列表 (例如: ["id"], ["food", "water"])
     */
    private List<String> keys;

    /**
     * 固定宽度 (例如: 12, 18)
     */
    private Integer fixedWidth;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Integer getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(Integer fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    
}

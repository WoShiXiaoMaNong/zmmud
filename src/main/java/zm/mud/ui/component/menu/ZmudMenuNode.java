package zm.mud.ui.component.menu;

import java.util.List;

/**
 * 菜单配置节点 POJO 
 */
public record ZmudMenuNode(
    String title,
    ZmudMenuType type,
    String menuDialog,
    List<ZmudMenuNode> children
) {
    // 可以在这里增加一个快速判断是否包含子项的辅助方法
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public String toString() {
        return "ZmudMenuNode{" +
                "title='" + title + '\'' +
                ", type=" + type +
                ", menuDialog='" + menuDialog + '\'' +
                ", children=" + children +
                '}';
    }

    
}

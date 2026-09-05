package zm.mud.ui.component.menu;

/**
 * MUD 菜单节点类型定义
 */
public enum ZmudMenuType {
    
    /**
     * 菜单目录 / 多级子菜单的父节点 (对应 JSON 中的 "menu")
     */
    MENU("menu"),
    
    /**
     * 叶子菜单项 / 具体功能按钮 (对应 JSON 中的 "menuItem")
     */
    MENU_ITEM("menuItem"),
    
    /**
     * 菜单分隔线 (对应 JSON 中的 "separator")
     */
    SEPARATOR("separator");

    private final String value;

    ZmudMenuType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据 JSON 中的字符串类型安全地转换成枚举
     * @param value JSON 里的 type 字符串
     * @return 匹配的枚举类型，若未匹配则默认返回 MENU_ITEM
     */
    public static ZmudMenuType fromValue(String value) {
        if (value != null) {
            for (ZmudMenuType type : ZmudMenuType.values()) {
                if (type.value.equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
        }
        return MENU_ITEM; // 默认防错处理
    }
}

package zm.mud.ui.component.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson2.TypeReference;

import zm.mud.core.cfg.CustomCfgLoader;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.MudMainScreen;

/**
 * 符合 MUD 风格的菜单栏
 * 整体背景与主菜单按钮均保留 Swing 默认系统颜色，仅在悬停时触发深灰高亮
 */
public class MudMenuBar extends JMenuBar {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(MudMenuBar.class);

    // 悬停与子菜单的配色定义
    private final Color borderColor = new Color(50, 50, 50); // 菜单栏底部的暗灰分隔线

    private final Font menuFont = new Font("SimSun", Font.PLAIN, 13); // 复古宋体
    private GlobalCfg globleCfg;
    private MudMainScreen mainScreen;


    public MudMenuBar(MudMainScreen mainScreen, GlobalCfg globleCfg) {
        this.mainScreen = mainScreen;
        this.globleCfg = globleCfg;
        this.initStyle();
        this.createMenus();
    }

    /**
     * 初始化菜单栏自身的样式
     */
    private void initStyle() {
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
    }

    /**
     * 构建具体的菜单项
     */
    private void createMenus() {
        List<ZmudMenuNode> menuNodes = (List<ZmudMenuNode>) CustomCfgLoader.loadUIConfig("menu", "menus", new TypeReference<List<ZmudMenuNode>>() {});
        
        List<JMenu> topMenus = new ArrayList<>();
        for(ZmudMenuNode node : menuNodes) {
            JMenu topMenu = createTopMenu(node);
            topMenus.add(topMenu);
            this.createMenu(node.children(), topMenu);
            this.add(topMenu);
        }
        
    }

    private void createMenu(List<ZmudMenuNode> menuNodes, JMenu parent) {
        if(menuNodes == null || menuNodes.isEmpty()) {
            return;
        }
        for (ZmudMenuNode node : menuNodes) {
            if(ZmudMenuType.SEPARATOR.equals(node.type())) {
                parent.addSeparator();
                continue;
            }
            boolean isLeafNode = !node.hasChildren() && ZmudMenuType.MENU_ITEM.equals(node.type());
            if(isLeafNode) {
                ZmudMenuItem menuItem = createMenuItem(node);
                parent.add(menuItem);
            } else {
                JMenu subMenu = createSubMenu(node.title());
                parent.add(subMenu);
                createMenu(node.children(), subMenu); // 递归创建子菜单
            }
        }
    }

  

    /**
     * 创建顶级主菜单按钮（保留系统默认颜色，悬停时变深灰）
     */
    private JMenu createTopMenu(ZmudMenuNode node) {
        JMenu menu = new JMenu(node.title());
        menu.setFont(menuFont);

        // 鼠标悬停监听
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (!menu.isPopupMenuVisible()) {
                    menu.setSelected(false);
                    menu.getModel().setSelected(false);
                    menu.getModel().setRollover(false);
                }
                menu.revalidate();
                menu.repaint();
            }
        });
        return menu;
    }

    /**
     * 创建普通叶子子菜单项
     */
    private ZmudMenuItem createMenuItem(ZmudMenuNode node) {
        try{
            
            ZmudMenuItem item = ZmudMenuItem.createMenuItem(node, menuFont,this.mainScreen);
            applySubMenuStyles(item);
            return item;
        } catch (Exception e) {
            logger.error("Failed to create menu item for node: " + node, e);
        }
       
        return null;
    }

    /**
     * 重载方法：创建子菜单项，支持将其作为“多级菜单的父节点” (JMenu)
     * @param isSubMenuParent 是否包含下一级子菜单
     */
    private JMenu createSubMenu(String title) {
        JMenu jMenu = new JMenu(title);
        applySubMenuStyles(jMenu);
        return jMenu;
    }

    /**
     * 抽取公共的子菜单/多级菜单样式与事件绑定逻辑
     */
    private void applySubMenuStyles(JMenuItem item) {
        item.setFont(menuFont);
        item.setOpaque(false); // 初始状态关闭显式不透明，让其跟随系统 UI 渲染
        item.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

        // 子菜单悬停交互
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // 如果是含有子菜单的 JMenu，且其弹窗正显示，则先不重置状态
                if (item instanceof JMenu && ((JMenu) item).isPopupMenuVisible()) {
                    return;
                }
                item.setSelected(false);
                item.getModel().setSelected(false);
                item.getModel().setRollover(false);
                item.revalidate();
                item.repaint();
            }
        });
    }
}

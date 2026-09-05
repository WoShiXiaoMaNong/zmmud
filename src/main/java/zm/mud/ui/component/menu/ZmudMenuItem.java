package zm.mud.ui.component.menu;

import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZmudMenuItem extends JMenuItem {
    private static final Logger logger = LogManager.getLogger(ZmudMenuItem.class);


    private  AbsZmMudDialog dialog;

    protected ZmudMenuItem(String text,Font font) {
        super(text);
        this.setFont(font);
        this.addListener();
       
    }

    public static ZmudMenuItem createMenuItem(ZmudMenuNode node, Font font,Frame owner) {
        try {
            ZmudMenuItem menuItem = new ZmudMenuItem(node.title(), font);
            Class<?> dialogClass = Class.forName(node.menuDialog());
            if (AbsZmMudDialog.class.isAssignableFrom(dialogClass)) {
                AbsZmMudDialog dialog = (AbsZmMudDialog) dialogClass.getConstructor(Frame.class, String.class).newInstance(owner, node.title());
                menuItem.setDialog(dialog);

            } else {
                throw new IllegalArgumentException("Class " + node.menuDialog() + " is not a subclass of AbsZmudDialog");
            }
            return menuItem;
        } catch (Exception e) {
            logger.error("Failed to create menu item for class: " + node.menuDialog(), e);
        }
        return null;
    }


    private void setDialog(AbsZmMudDialog dialog) {
        this.dialog = dialog;
    }

    private void addListener() {
        JMenuItem self = this;
         // 子菜单悬停交互
        self.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // 如果是含有子菜单的 JMenu，且其弹窗正显示，则先不重置状态
                if (self instanceof JMenu && ((JMenu) self).isPopupMenuVisible()) {
                    return;
                }
                self.setSelected(false);
                self.getModel().setSelected(false);
                self.getModel().setRollover(false);
                self.revalidate();
                self.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dialog != null) {
                    dialog.setVisible(true);
                }
            }

            
        });
    }
}

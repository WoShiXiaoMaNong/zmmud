package zm.mud.ui.component.menu;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbsZmudMenuItem extends JMenuItem {
    private static final Logger logger = LogManager.getLogger(AbsZmudMenuItem.class);

    protected AbsZmudMenuItem(String text,Font font) {
        super(text);
        this.setFont(font);
        this.addListener();
    }

    public static AbsZmudMenuItem createMenuItem(String className, String text, Font font) {
        try {
            Class<?> clazz = Class.forName(className);
            if (AbsZmudMenuItem.class.isAssignableFrom(clazz)) {
                return (AbsZmudMenuItem) clazz.getConstructor(String.class, Font.class).newInstance(text, font);
            } else {
                throw new IllegalArgumentException("Class " + className + " is not a subclass of AbsZmudMenuItem");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                onClick(e);
            }

            
        });
    }


    protected abstract void onClick(MouseEvent e);
    
}

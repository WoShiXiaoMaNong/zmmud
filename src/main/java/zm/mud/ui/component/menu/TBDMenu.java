package zm.mud.ui.component.menu;

import java.awt.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TBDMenu extends AbsZmMudDialog {
    private static final Logger logger = LogManager.getLogger(TBDMenu.class);

    public TBDMenu(Frame owner, String title) {
        super(owner, title); 
    }

    @Override
    protected JPanel getContentPanelUi() {
        // 主面板采用 BorderLayout
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        
        // 创建一个居中对齐的提示标签，告知当前功能正在开发中
        JLabel tbdLabel = new JLabel("🔧 功能开发中 (To Be Done)...", SwingConstants.CENTER);
        
        // 设置一个稍微醒目且舒适的字体
        tbdLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        tbdLabel.setForeground(Color.GRAY);
        
        mainContentPanel.add(tbdLabel, BorderLayout.CENTER);
        
        return mainContentPanel;
    }

    @Override
    protected void ok() {
        // TBD 默认无需复杂处理，记录一条调试日志即可
        logger.info("点击了 TBD 窗口的确定按钮：[{}]", this.getTitle());
    }
}

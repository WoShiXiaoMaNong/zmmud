package zm.mud.ui.component.menu.setting.timer;


import java.awt.*;
import javax.swing.*;
import zm.mud.ui.component.menu.AbsZmMudDialog;

/**
 * 客户端全局主配置对话框窗口
 */
public class TimerConfigMenu extends AbsZmMudDialog {

    private TimerConfigView configView; 
    private TimerConfigPresenter presenter; 

    public TimerConfigMenu(Frame owner, String title) {
        super(owner, title);
        // 纯全局字体配置界面，调整为更精致小巧的固定尺寸
        this.setSize(400, 220);
        this.setResizable(false);
        this.setLocationRelativeTo(owner);
    }

    @Override
    protected JPanel getContentPanelUi() {
        // 1. 实例化原子 UI 组件
        configView = new TimerConfigView();

        // 2. 构建主大容器
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        // 给予四周舒适优雅的留白
        gbc.insets = new Insets(12, 16, 12, 16); 
        gbc.fill = GridBagConstraints.BOTH;

        // ─── 布局：全局配置编辑区直接占据核心工作区 ───
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // 填满弹窗主空间
        mainPanel.add(configView, gbc);

        // 3. 初始化全局控制中枢 Presenter
        this.presenter = new TimerConfigPresenter(configView);

        // 4. 首次加载与数据回显（遵循 Hierarchy 挂载触发标准）
        mainPanel.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && mainPanel.isShowing()) {
                    // 通过 presenter 触发全局加载
                    presenter.loadConfig();
                    
                    // 刷新一次后即移除监听，避免后续重复触发
                    mainPanel.removeHierarchyListener(this);
                }
            }
        });

        return mainPanel;
    }

    /**
     * 当玩家点击对话框自带的“确定”按钮时，由父类自动回调
     */
    @Override
    protected void ok() {
        if (presenter != null) {
            presenter.saveConfig();
        }
    }
}
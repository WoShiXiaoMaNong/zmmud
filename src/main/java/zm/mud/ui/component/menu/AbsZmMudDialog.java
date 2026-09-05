package zm.mud.ui.component.menu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public abstract class AbsZmMudDialog extends JDialog {
    public AbsZmMudDialog(Frame owner, String title) {
         // 第三个参数为 true 表示模态窗口，会锁定主界面
        super(owner, title, true); 
        
        // 1. 设置基础属性
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // 关闭时销毁窗口释放内存
        this.setSize(700, 500);                                  // 设置标准大小
        this.setLayout(new BorderLayout());
        
        // 2. 将核心内容面板塞入中间
        this.add(getContentPanelUi(), BorderLayout.CENTER);
        
        // 3. 创建底部的通用的 [确定] [取消] 按钮区域
       JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
       buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JButton btnOk = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        // 4. 绑定基础按钮事件
        btnCancel.addActionListener(e -> dispose()); // 关闭弹窗
        btnOk.addActionListener(e -> {
            this.ok();
            this.dispose();
        });
        
        // 5. 窗口居中（必须在设置了 size 之后调用）
        this.setLocationRelativeTo(owner);
    }


    protected abstract JPanel getContentPanelUi();

    protected abstract void ok();
}

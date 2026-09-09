package zm.mud.ui.component;

import java.awt.Component;
import java.awt.event.AdjustmentListener;
import java.util.function.Function;

import javax.swing.JScrollBar;

public class MudScrollPane extends javax.swing.JScrollPane {

    private Function<Boolean, Component> manualAdjustmentHandler;

    public MudScrollPane() {
        super();
        this.init();
    }
    public MudScrollPane(Component view, Function<Boolean/* is bottom */, Component> manualAdjustmentHandler) {
        super(view);
        this.manualAdjustmentHandler = manualAdjustmentHandler;
        this.init();
    }

    private void init(){

        JScrollBar verticalScrollBar = this.getVerticalScrollBar();

        verticalScrollBar.addAdjustmentListener(new AdjustmentListener(){
            @Override
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
                // 当滚动条被拖动时，自动滚动到底部
                if (verticalScrollBar.getValueIsAdjusting()) {
                    manualAdjustmentHandler.apply(false); // 调用外部传入的处理函数
                }
            }
        });

         // 2. 监听鼠标滚轮事件（额外保险，防止有些平台拖拽不触发 adjusting）
        this.addMouseWheelListener(e -> {
            manualAdjustmentHandler.apply(false); // 调用外部传入的处理函数
            // 检查滚完之后是否回到了最底部
            checkIfAtBottom(verticalScrollBar);
        });

        // 3. 实时检测是否回到了底部。如果用户手动拉回底部，自动恢复自动滚动
        verticalScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
                // 如果用户没有在拖拽了，检测位置
                if (!verticalScrollBar.getValueIsAdjusting()) {
                    checkIfAtBottom(verticalScrollBar);
                }
            }
        });
    }

    /**
     * 判断滚动条是否处于最底部
     */
    private void checkIfAtBottom(JScrollBar verticalBar) {
        int extent = verticalBar.getModel().getExtent();
        int maximum = verticalBar.getMaximum();
        int value = verticalBar.getValue();
        
        // 允许 5 像素的误差（防止部分系统缩放导致计算不精准）
        if (value + extent >= maximum - 10) {
            manualAdjustmentHandler.apply(true); // 调用外部传入的处理函数
        }
    }

}

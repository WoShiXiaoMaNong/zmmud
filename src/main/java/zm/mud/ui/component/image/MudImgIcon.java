package zm.mud.ui.component.image;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


public class MudImgIcon extends JLabel {
    private String imgOriginUrl;
    private ImageIcon imageIcon;



    public MudImgIcon(ImageIcon imageIcon,String imgOriginUrl,BiConsumer<MouseEvent,MudImgIcon> onDoubleClick) {
        super( imageIcon);
        this.imgOriginUrl = imgOriginUrl;
        this.imageIcon = imageIcon;
        MudImgIcon self = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && onDoubleClick != null) {
                    onDoubleClick.accept(e, self);
                }
            }
        });
    }
    public String getImgOriginUrl() {
        return imgOriginUrl;
    }
    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    
}

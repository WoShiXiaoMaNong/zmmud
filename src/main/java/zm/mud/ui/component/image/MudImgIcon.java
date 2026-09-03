package zm.mud.ui.component.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;



import zm.mud.core.session.MudSession;


public class MudImgIcon extends JLabel {
    private String imgOriginUrl;
    private ImageIcon imageIcon;
    private MudSession session;


    public MudImgIcon(MudSession session,ImageIcon imageIcon,String imgOriginUrl,BiConsumer<MouseEvent,MudImgIcon> onClick) {
        super( imageIcon);
        this.session = session;
        this.imgOriginUrl = imgOriginUrl;
        this.imageIcon = imageIcon;
        this.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        MudImgIcon self = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.accept(e, self);
                }
            }
        });
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    }

    public MudSession getSession() {
        return session;
    }
    public String getImgOriginUrl() {
        return imgOriginUrl;
    }
    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    
}

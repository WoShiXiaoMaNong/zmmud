package zm.mud.ui.component.image;

import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import zm.mud.ui.component.MudPopup;

public class ImageDoubleClickListener implements BiConsumer<MouseEvent, MudImgIcon> {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ImageDoubleClickListener.class);

    @Override
    public void accept(MouseEvent e, MudImgIcon mudImgIcon) {
        boolean isDoubleClick = SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2;
        if (!isDoubleClick) {
            return;
        }

        String url = mudImgIcon.getImgOriginUrl();
        if (url == null || url.isEmpty()) {
            return;
        }

        MudPopup.popupImg(mudImgIcon.getSession(), mudImgIcon);

    }

}

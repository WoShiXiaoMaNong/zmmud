package zm.mud.ui.component;

import java.awt.image.BufferedImage;

public class ImageInfo {
    private String imgUrl;
    private boolean insertMode;

    private BufferedImage bufferedImage;
    
    public ImageInfo(String imgUrl, boolean insertMode) {
        this.imgUrl = imgUrl;
        this.insertMode = insertMode;
    }
    public String getImgUrl() {
        return imgUrl;
    }
    public boolean isInsertMode() {
        return insertMode;
    }

    
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
    @Override
    public String toString() {
        return "ImageInfo [imgUrl=" + imgUrl + ", insertMode=" + insertMode + "]";
    }

    
}

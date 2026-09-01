package zm.mud.ui.component.image;

import java.awt.image.BufferedImage;

public class ImageInfo {
    private String imgUrl;
    private boolean insertMode;

    private BufferedImage bufferedImage;
    
    private boolean needBeforeNewLine; //显示前是否换行
    private boolean needNewLine ;  // 显示后是否换行

    private int maxWidth;

    public ImageInfo(String imgUrl, boolean insertMode) {
        this(imgUrl, insertMode, true);
    }

    public ImageInfo(String imgUrl, boolean insertMode, boolean needNewLine) {
        this.imgUrl = imgUrl;
        this.insertMode = insertMode;
        this.needNewLine = needNewLine;
        this.maxWidth = -1; // 默认不限制宽度
        this.needBeforeNewLine = false; // 默认不换行
    }

    
    public boolean isNeedBeforeNewLine() {
        return needBeforeNewLine;
    }

    public void setNeedBeforeNewLine(boolean needBeforeNewLine) {
        this.needBeforeNewLine = needBeforeNewLine;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public boolean isInsertMode() {
        return insertMode;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean isNeedNewLine() {
        return needNewLine;
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

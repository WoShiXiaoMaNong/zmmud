package zm.mud.ui.util;

import java.awt.image.BufferedImage;


public class ImageUtil {
    /**
     * 等比例缩放图片
     * 
     * @param srcImage   原图
     * @param maxWidth   允许的最大宽度（像素）
     * @return 缩放后的新图片
     */
    public static BufferedImage scaleImage(BufferedImage srcImage, int maxWidth) {
        int originalWidth = srcImage.getWidth();
        int originalHeight = srcImage.getHeight();

        // 如果原图宽度已经小于等于最大宽度，无需缩放，直接返回原图
        if (originalWidth <= maxWidth) {
            return srcImage;
        }

        // 计算等比例缩小后的高度
        double scaleRatio = (double) maxWidth / originalWidth;
        int newWidth = maxWidth;
        int newHeight = (int) (originalHeight * scaleRatio);

        // 创建高质量缩放的新图片画布
        // 根据原图是否有透明通道，选择不同的图片类型
        int imageType = (srcImage.getType() == BufferedImage.TYPE_CUSTOM) ? 
                        BufferedImage.TYPE_INT_ARGB : srcImage.getType();
                        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, imageType);
        
        // 渲染缩放后的图片
        java.awt.Graphics2D g2d = scaledImage.createGraphics();
        // 开启双线性插值抗锯齿，保证缩放后的图片清晰、不模糊 [1]
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                            java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(srcImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaledImage;
    }

}

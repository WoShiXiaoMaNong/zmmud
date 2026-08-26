package zm.mud.ui.component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.ui.util.AnsiToStyleDocUtil;
import zm.mud.utils.HttpUtil;
import zm.mud.utils.SpringBeanUtil;

public class MudTextAare extends JTextPane {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudTextAare.class);

    // 新增：引入 50 行的缓冲区，避免每来一行就执行删除导致的界面抖动
    private static final int BUFFER_LINES = 200;

    private StyledDocument doc;
    private AnsiToStyleDocUtil ansiToStyleDocUtil;

    private GlobleCfg globleCfg;

    private Lock printLock;

    private int displayBufLineNumber;

    public MudTextAare(GlobleCfg cfg) {
        this.printLock = new ReentrantLock();
        this.globleCfg = cfg;
        this.displayBufLineNumber = cfg.getDisplayBufLineNumber();
        this.setEditable(false);
        this.setBackground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setForeground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setParagraphAttributes(this.getParagraphAttributes(), true);
        this.doc = this.getStyledDocument();
        this.ansiToStyleDocUtil = ZmMudUI.getContext().getBean(AnsiToStyleDocUtil.class);
        logger.info("displayBufLineNumber :" + this.displayBufLineNumber );
    }

    /**
     * 
     * @param text
     */
    public void printlnToScreen(String text) {
        this.printlnToScreen(text, false);
    }

    /**
     * 
     * @param text
     * @param enableBlod 使用加粗来表示 高亮 ，注意，会引起字符无法对齐问题！！！
     */
    public void printlnToScreen(String text, boolean enableBlod) {
        SwingUtilities.invokeLater(() -> {
            printLock.lock();
            try {
                ansiToStyleDocUtil.parseAnsiToStyledDocument(text + "\r\n", doc, this.globleCfg.getFont(),
                        this.globleCfg.getThemeType().getTheme(), enableBlod);
                trimLines();
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print to screen", e);
            } finally {
                printLock.unlock();
            }
        });
    }

    public void printImg(List<ImageInfo> imgUrls) {
        this.printImg(imgUrls, doc.getLength());
    }

    /**
     * 在offset所在行的下一行开始输出
     * 
     * @param imgUrl
     * @param offset
     */
    public void printImg(List<ImageInfo> imgUrls, int offset) {

        try {
            List<ImageInfo> fetchSucceedImages = new ArrayList<>();
            for (ImageInfo imageInfo : imgUrls) {
                logger.info("开始下载 MUD 图片: " + imageInfo);
                HttpUtil httpUtil = SpringBeanUtil.getBean(HttpUtil.class);
                BufferedImage image = httpUtil.download(imageInfo.getImgUrl(),
                        new Function<InputStream, BufferedImage>() {
                            @Override
                            public BufferedImage apply(InputStream inputStream) {
                                try {
                                    return javax.imageio.ImageIO.read(inputStream);
                                } catch (IOException e) {
                                    logger.error("下载图片失败！", e);
                                }
                                return null;
                            }
                        }, BufferedImage.class);

                if (image == null) {
                    logger.error("图片解析失败，未获得有效图像数据: " + imageInfo);
                    printErrorToScreenAsync("[图片解析失败]");
                    continue;
                }
                imageInfo.setBufferedImage(image);
                fetchSucceedImages.add(imageInfo);
            }

            if (fetchSucceedImages.size() == 0) {
                logger.error("图片解析失败，未获得有效图像数据. ");
                return;
            }

            // 5. 切回 UI 线程，并严格加锁写入 JTextPane
            SwingUtilities.invokeLater(() -> {
                printLock.lock(); // 加锁：保证图文严格的时序
                try {
                    int nextOffset = offset;
                    for (ImageInfo image : fetchSucceedImages) {
                        // 获取图片原始高度（像素）
                        int imageHeight = image.getBufferedImage().getHeight();

                        // 4. 下载成功！构建 ImageIcon
                        ImageIcon imageIcon = new ImageIcon(image.getBufferedImage());
                        nextOffset = this.doImageInsert(imageIcon, image.isInsertMode(), nextOffset, imageHeight);

                    }

                    // 4. 触发你原有的行数裁剪逻辑
                    trimLines();

                    // 5. 保持良好体验：滚动条自动滚动到最下方最新消息
                    this.setCaretPosition(doc.getLength());

                } catch (BadLocationException e) {
                    logger.error("Failed to process image to doc in cover mode", e);
                } finally {
                    printLock.unlock(); // 释放锁
                }
            });

        } catch (Exception e) {
            logger.error("下载 MUD 图片失败: " + imgUrls, e);
            printErrorToScreenAsync("[图片下载失败]");
        }
    }

    private int doImageInsert(ImageIcon imageIcon, boolean insertMode, int offset, int imageHeight)
            throws BadLocationException {

        int baseOffset = offset;

        // 兜底：如果完全没找到对应的 URL 或者超出范围，则强制降级放到最后
        if (baseOffset == -1 || baseOffset > doc.getLength()) {
            baseOffset = doc.getLength();
        }


        if (insertMode) {
            // ====================================================
            // 【1. 插入模式】：从后一行开始插入，不破坏任何原有文本
            // ====================================================
            this.setCaretPosition(baseOffset);
            this.insertIcon(imageIcon);
            doc.insertString(this.getCaretPosition(), "\n", null);

        } else {
            // ====================================================
            // 【2. 覆盖模式】：动态计算图片占用的行数，并将其全部覆盖（删除）
            // ====================================================
            
            // --- 【定位当前行和下一行】 ---
            javax.swing.text.Element root = doc.getDefaultRootElement();
            int currentLineIndex = root.getElementIndex(baseOffset);
            javax.swing.text.Element currentLineElem = root.getElement(currentLineIndex);

            // 当前行的结束位置（即后一行的开始位置）
            int nextLineStartOffset = currentLineElem.getEndOffset();

            if (nextLineStartOffset > doc.getLength()) {
                nextLineStartOffset = doc.getLength();
            }

            int nextLineIndex = currentLineIndex + 1;

            // 1. 根据当前字体动态计算单行文本的像素高度
            java.awt.FontMetrics metrics = this.getFontMetrics(this.getFont());
            int lineHeight = metrics.getHeight();
            if (lineHeight <= 0) {
                lineHeight = 16; // 容错兜底高度
            }

            // 2. 计算图片实际会撑开的文本行数（向上取整，例如 5.3 行算作 6 行）
            int linesToCover = (int) Math.ceil((double) imageHeight / lineHeight);
            logger.debug("图片高度: " + imageHeight + "px, 单行高: " + lineHeight + "px, 预计覆盖行数: " + linesToCover);

            // 3. 确定被覆盖区域的终点行
            int endLineIndex = nextLineIndex + linesToCover - 1;
            int totalLines = root.getElementCount();
            if (endLineIndex >= totalLines) {
                endLineIndex = totalLines - 1; // 确保不越界
            }

            // 4. 如果确实有需要覆盖的行，计算它们的总字符长度并执行删除
            if (nextLineIndex < totalLines && endLineIndex >= nextLineIndex) {
                javax.swing.text.Element endLineElem = root.getElement(endLineIndex);
                int coverEndOffset = endLineElem.getEndOffset();
                int lengthToRemove = coverEndOffset - nextLineStartOffset;

                if (lengthToRemove > 0) {
                    // 一口气删掉接下来的多行文本
                    doc.remove(nextLineStartOffset, lengthToRemove);
                }
            }

            // 5. 在清除出来的干净区域插入图片
            this.setCaretPosition(nextLineStartOffset);
            this.insertIcon(imageIcon);

            // 补回一个换行符，确保后续未被覆盖的文本能正常排在图片下方
            doc.insertString(this.getCaretPosition(), "\n", null);
        }

        return this.getCaretPosition(); // 返回插入image后最新位置
    }

    /**
     * 辅助方法：当图片下载/解析失败时，安全地在屏幕上打印错误提示
     */
    private void printErrorToScreenAsync(String errorMsg) {
        SwingUtilities.invokeLater(() -> {
            printLock.lock();
            try {
                doc.insertString(doc.getLength(), errorMsg + "\r\n", null);
                trimLines();
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print error msg to screen", e);
            } finally {
                printLock.unlock();
            }
        });
    }

    private void trimLines() throws BadLocationException {
        javax.swing.text.Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();

        // 优化：只有当总行数超过 最大限制 + 缓冲限制 (150行) 时，才集中清理一次
        if (lineCount <= this.displayBufLineNumber + BUFFER_LINES) {
            return;
        }

        // 需要删除的行数
        int linesToRemove = lineCount - this.displayBufLineNumber;

        // 找到第 N 行的结束位置
        javax.swing.text.Element lineElement = root.getElement(linesToRemove - 1);
        int endOffset = lineElement.getEndOffset();

        // 删除从开头到这个位置的内容
        doc.remove(0, endOffset);
    }

    public int getMsgOffset(String msg) {
        try {
            // 获取当前文档的总长度
            int docLength = doc.getLength();
            // 提取整篇文档的纯文本内容
            String text = doc.getText(0, docLength);

            // 查找 URL 的起始位置
            int index = text.indexOf(msg);
            if (index != -1) {
                // 返回 URL 的结束位置（起始位置 + 字符串长度）
                return index + msg.length();
            }
        } catch (Exception e) {
            logger.error("Error reading text from document", e);
        }
        return -1;
    }

}

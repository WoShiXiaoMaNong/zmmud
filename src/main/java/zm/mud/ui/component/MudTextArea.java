package zm.mud.ui.component;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import zm.mud.core.session.MudSession;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.ui.component.image.ImageInfo;
import zm.mud.ui.component.image.MudImgIcon;
import zm.mud.ui.util.AnsiToStyleDocUtil;
import zm.mud.utils.HttpUtil;
import zm.mud.utils.SpringBeanUtil;

public class MudTextArea extends JTextPane {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudTextArea.class);

    // 新增：引入 50 行的缓冲区，避免每来一行就执行删除导致的界面抖动
    private static final int BUFFER_LINES = 200;

    private final SimpleAttributeSet errorStyle;

    private StyledDocument doc;
    private AnsiToStyleDocUtil ansiToStyleDocUtil;

    private GlobalCfg globleCfg;

    private MudSession session;

    private int displayBufLineNumber;

    private volatile boolean isAutoScrollEnabled = true; // 默认启用自动滚动

    public MudTextArea(MudSession session, GlobalCfg cfg) {
        this.session = session;
        this.globleCfg = cfg;
        this.displayBufLineNumber = cfg.getDisplayBufLineNumber();
        this.setEditable(false);
        this.setBackground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setForeground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setParagraphAttributes(this.getParagraphAttributes(), true);
        this.doc = this.getStyledDocument();
        this.ansiToStyleDocUtil = ZmMudUI.getContext().getBean(AnsiToStyleDocUtil.class);
        this.errorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(errorStyle, Color.RED);

        logger.info("displayBufLineNumber :" + this.displayBufLineNumber);

    }

    public MudSession getSession() {
        return session;
    }

    /**
     * 
     * @param text
     */
    public void printlnToScreen(String text) {
        this.printlnToScreen(text, false);
    }

    public void setAutoScrollEnabled(boolean enabled) {
        // 统一在主线程切换状态，并立即同步更新光标策略
        if (SwingUtilities.isEventDispatchThread()) {
            this.isAutoScrollEnabled = enabled;
            this.syncCaretPolicy(); // 状态改变时，立刻调整策略
        } else {
            SwingUtilities.invokeLater(() -> {
                this.isAutoScrollEnabled = enabled;
                this.syncCaretPolicy();
            });
        }
    }

    @Override
    public void setCaretPosition(int position) {
        try {
            // 1. 确保策略是正确的
            this.syncCaretPolicy();
            
            // 2. 调用父类方法移动光标（如果是 ALWAYS_UPDATE，此时会触发一次基于当前已知布局的滚动）
            super.setCaretPosition(position);
            
            // 3. 核心补丁：解决图片异步撑开导致的滚动不到位问题
            if (this.isAutoScrollEnabled) {
                // 使用双层 InvokeLater，确保在文档插入、图片标签生成、以及外层 JScrollPane 重新布局（validate）全部完成后执行
                SwingUtilities.invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        // 检查当前光标是否依然在文档末尾（防止用户在此期间已经手动点到了别的地方）
                        if (this.isAutoScrollEnabled && this.getCaretPosition() == this.getDocument().getLength()) {
                            try {
                                // 获取末尾真实的几何矩形（此时图片已经撑开，高度绝对准确）
                                java.awt.Rectangle rect = this.modelToView2D(this.getDocument().getLength()).getBounds();
                                // 稍微向下加一点冗余高度，确保安全彻底地置底
                                rect.y += rect.height + 10;
                                this.scrollRectToVisible(rect);
                            } catch (BadLocationException e) {
                                // 降级备用方案：如果上面由于索引问题报错，直接强行按组件当前实际总高度的最底部进行滚动
                                java.awt.Rectangle rect = new java.awt.Rectangle(0, this.getHeight() - 1, 1, 1);
                                this.scrollRectToVisible(rect);
                            }
                        }
                    });
                });
            }
            
        } catch (Exception e) {
            logger.error("Failed to set caret position", e);
        }
    }

    private void syncCaretPolicy() {
        javax.swing.text.Caret caret = this.getCaret();
        if (caret instanceof javax.swing.text.DefaultCaret) {
            javax.swing.text.DefaultCaret defaultCaret = (javax.swing.text.DefaultCaret) caret;
            if (this.isAutoScrollEnabled) {
                // 开启自动滚动时：强制策略为 ALWAYS_UPDATE
                // 这样不管是插入新文本，还是手动调用 setCaretPosition，组件都会雷打不动地滚到最后
                defaultCaret.setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);
            } else {
                // 关闭自动滚动时：强制策略为 NEVER_UPDATE
                // 此时任何文本追增、光标赋值，底层都不会触发任何滚动，实现彻底锁定
                defaultCaret.setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
            }
        }
    }

    @Override
    public void scrollRectToVisible(java.awt.Rectangle aRect) {
        if (isAutoScrollEnabled) {
            super.scrollRectToVisible(aRect);
        }
    }

    /**
     * 
     * @param text
     * @param enableBlod 使用加粗来表示 高亮 ，注意，会引起字符无法对齐问题！！！
     */
    public void printlnToScreen(String text, boolean enableBlod) {
        SwingUtilities.invokeLater(() -> {
            try {
                ansiToStyleDocUtil.parseAnsiToStyledDocument(text + "\r\n", doc, this.globleCfg.getFont(),
                        this.globleCfg.getThemeType().getTheme(), enableBlod);
                trimLines();
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print to screen", e);
            }
        });
    }

    public void printImg(List<ImageInfo> imgUrls, BiConsumer<MouseEvent, MudImgIcon> onClick) {
        this.printImg(imgUrls, doc.getLength(), onClick);
    }

    /**
     * 在offset所在行的下一行开始输出
     * 
     * @param imgUrl
     * @param offset
     */
    public void printImg(List<ImageInfo> imgUrls, int offset, BiConsumer<MouseEvent, MudImgIcon> onClick) {

        try {
            List<ImageInfo> fetchSucceedImages = new ArrayList<>();
            for (ImageInfo imageInfo : imgUrls) {
                logger.debug("开始下载 MUD 图片: " + imageInfo);
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
                try {
                    int nextOffset = offset;
                    int docLength = doc.getLength();

                    // 确保 offset 在当前文档的合法范围内
                    if (nextOffset < 0) {
                        nextOffset = 0;
                    } else if (nextOffset > docLength) {
                        nextOffset = docLength; // 或者选择直接 return，不执行非法插入
                    }

                    for (ImageInfo image : fetchSucceedImages) {
                        // 获取图片原始高度（像素）
                        int imageHeight = image.getBufferedImage().getHeight();

                        // 4. 下载成功！构建 ImageIcon
                        if (image.getMaxWidth() > 0 && image.getBufferedImage().getWidth() > image.getMaxWidth()) {
                            // 如果设置了最大宽度，并且图片宽度超过了这个限制，则进行等比例缩放
                            BufferedImage scaledImage = zm.mud.ui.util.ImageUtil.scaleImage(image.getBufferedImage(),
                                    image.getMaxWidth());
                            image.setBufferedImage(scaledImage);
                            imageHeight = scaledImage.getHeight(); // 更新图片高度
                        }
                        ImageIcon imageIcon = new ImageIcon(image.getBufferedImage());
                        if (image.isNeedBeforeNewLine()) {
                            doc.insertString(nextOffset, "\n", null);
                            nextOffset++;
                        }
                        nextOffset = this.doImageInsert(imageIcon, image.isInsertMode(), nextOffset, imageHeight,
                                image.getImgUrl(), onClick);

                        if (image.isNeedNewLine()) {
                            // 在图片的精准屁股后面补上换行符
                            doc.insertString(nextOffset, "\n", null);
                            nextOffset++;
                        }
                    }

                    // 4. 触发你原有的行数裁剪逻辑
                    // trimLines();

                    // 5. 保持良好体验：滚动条自动滚动到最下方最新消息
                    int targetOffset = doc.getLength();
                    if (targetOffset >= 0) {
                        // 核心：让 Swing 强制将 targetOffset 所在的坐标（即最后一张图的换行符位置）滚动到可见区域
                        java.awt.Rectangle modelToViewRect = this.modelToView2D(targetOffset).getBounds();
                        if (modelToViewRect != null) {
                            // 适当增加一些底部留白高度（例如加上最后一张图的大概高度或30像素），确保整张图完全露出来
                            modelToViewRect.y += 30;
                            this.scrollRectToVisible(modelToViewRect);
                        } else {
                            // 降级兜底方案
                            this.setCaretPosition(targetOffset);
                        }
                    }

                    trimLines();
                    this.setCaretPosition(doc.getLength());
                    // =======================================================
                } catch (BadLocationException e) {
                    logger.error("Failed to process image to doc in cover mode,Offset:" + offset, e);
                }
            });

        } catch (Exception e) {
            logger.error("下载 MUD 图片失败: " + imgUrls, e);
            printErrorToScreenAsync("[图片下载失败]");
        }
    }

    private int doImageInsert(ImageIcon imageIcon, boolean insertMode, int offset, int imageHeight, String originUrl,
            BiConsumer<MouseEvent, MudImgIcon> onClick)
            throws BadLocationException {

        int baseOffset = offset;

        int currentDocLength = doc.getLength();
        // 兜底：如果完全没找到对应的 URL 或者超出范围，则强制降级放到最后
        if (baseOffset == -1 || baseOffset > currentDocLength) {
            baseOffset = currentDocLength;
        }

        if (insertMode) {
            // ====================================================
            // 【1. 插入模式】：从后一行开始插入，不破坏任何原有文本
            // ====================================================
            this.setCaretPosition(baseOffset);

            MudImgIcon imageLabel = new MudImgIcon(this.getSession(), imageIcon, originUrl, onClick);
            this.insertComponent(imageLabel);

            // 明确计算：插入一个 Icon 占用 1 个字符长度
            return baseOffset + 1;

        } else {
            // ====================================================
            // 【2. 覆盖模式】：动态计算图片占用的行数，并将其全部覆盖（删除）
            // ====================================================

            // 假设你原本计算出的需要删除的长度是某个值，这里用你的变量替代，此处假设为 lengthToRemove
            int lengthToRemove = 1; // 【请将此处替换为您原代码里计算出来的删除长度变量】

            // 核心防御 2：严格边界检查，防止 trimLines 裁剪后引发的 BadLocationException
            if (baseOffset + lengthToRemove > currentDocLength) {
                // 如果要删除的区间越界了，动态截断，只删除到文档末尾
                lengthToRemove = currentDocLength - baseOffset;
            }

            // 只有当有内容可删，且起点合法时才执行 remove
            if (baseOffset >= 0 && lengthToRemove > 0) {
                try {
                    doc.remove(baseOffset, lengthToRemove);
                } catch (BadLocationException e) {
                    logger.error("Failed to remove text for image cover. Offset: " + baseOffset + ", Length: "
                            + lengthToRemove + ", DocLength: " + currentDocLength, e);
                    // 降级处理：既然删除失败，把写入点直接修正到当前末尾，避免后续 insert 跟着连带崩溃
                    baseOffset = doc.getLength();
                }
            } else if (baseOffset < 0) {
                baseOffset = 0;
            }

            // 重新获取 remove 之后的最新安全长度
            baseOffset = Math.min(baseOffset, doc.getLength());

            // 在安全位置插入图片
            this.setCaretPosition(baseOffset);

            MudImgIcon imageLabel = new MudImgIcon(this.getSession(), imageIcon, originUrl, onClick);
            this.insertComponent(imageLabel);

            return baseOffset + 1;
        }
    }

    /**
     * 辅助方法：当图片下载/解析失败时，安全地在屏幕上打印错误提示
     */
    private void printErrorToScreenAsync(String errorMsg) {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), errorMsg + "\r\n", errorStyle);
                trimLines();
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print error msg to screen", e);
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

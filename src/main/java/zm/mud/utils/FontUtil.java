package zm.mud.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;


public class FontUtil {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FontUtil.class);

     public static void registerFont() {
        // 1. 定義 resources/mono/ 下所有的字型檔名清單（精準匹配，避開 Jar 包無法遍歷的問題）
        List<String> fontFiles = Arrays.asList(
                "sarasa-mono.ttf" // 請替換成你實際的檔名
        );

        // 2. 獲取 JVM 本地圖形環境
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // 3. 循環載入並註冊每一個字型
        for (String fileName : fontFiles) {
            String resourcePath = "/fonts/" + fileName; // 注意你的路徑對應 resources/mono/

            try (InputStream is = FontUtil.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("找不到字型資源檔: " + resourcePath);
                    continue;
                }

                // 載入字型
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

                // 註冊字型
                boolean success = ge.registerFont(baseFont);

                if (success) {
                    // 打印邏輯名稱與家族名稱，方便你後續在代碼中調用
                    logger.info("成功註冊字型 -> Name: " + baseFont.getName() + " | Family: " + baseFont.getFamily());
                } else {
                    logger.error("字型已存在或註冊失敗: " + fileName);
                }

            } catch (Exception e) {
                logger.error("載入字型失敗 [" + fileName + "] ",e);
            }
        }
    }

public static List<Font> getAllMonospacedFonts() {
    List<Font> monospacedFonts = new ArrayList<>();
    
    // 1. 获取系统中所有的物理字体
    Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    
    // 创建一个临时的 BufferedImage 以获取 FontMetrics 进行宽度测量
    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    // 🛠️ 统一基于 Family 级别去重（防止同一家族的粗体/斜体重复加入）
    Set<String> familyNames = new HashSet<>();
    
    // 2. 遍历并筛选等宽字体
    for (Font font : allFonts) {
        String family = font.getFamily();
        if (familyNames.contains(family)) {
            continue;
        }
        
        // 使用常规样式、16号大小进行测试（16号字在测量中英文字体比例时比12号更稳定，规避像素舍入误差）
        Font deriveFont = font.deriveFont(Font.PLAIN, 16);
        FontMetrics metrics = g2d.getFontMetrics(deriveFont);
        
        // 3. 西文等宽校验：比较窄字符 'i' 和宽字符 'm' 的像素宽度
        int widthI = metrics.charWidth('i');
        int widthM = metrics.charWidth('m');
        
        // 🛠️ 4. 中文等宽与双字节对齐校验：
        // 选取高频常用汉字 '中'（或 '猹' 等复杂字）进行测量
        int widthChinese = metrics.charWidth('中');
        
        // 核心筛选条件：
        // a. 英文部分本身等宽 (widthI == widthM)
        // b. 🛠️ 中文部分的宽度严格等于英文宽度的 2 倍 (widthChinese == widthM * 2)
        if (widthI == widthM && widthI > 0 && widthChinese == widthM * 2) {
            familyNames.add(family);
            monospacedFonts.add(font);
        }
    }
    
    g2d.dispose();
    return monospacedFonts;
}


    public static void refreshHistoricalTextFont(JTextPane textPane, String newFontName, int newFontSize) {
        if (textPane == null) return;

        // 一：所有针对 Swing UI 的历史重刷操作，必须强制切回 EDT 线程
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = textPane.getStyledDocument();
            int length = doc.getLength();
            
            if (length > 0) {
                // 构造全局一致的字体和字号属性
                SimpleAttributeSet fontAttrs = new SimpleAttributeSet();
                StyleConstants.setFontFamily(fontAttrs, newFontName);
                StyleConstants.setFontSize(fontAttrs, newFontSize);

                // 二：增量刷新（false）。只重置字体和大小，保留原有的 ANSI 颜色
                doc.setCharacterAttributes(0, length, fontAttrs, false);
                
                // 三：同步刷新底层的 Logical Style，防止部分自带段落样式的行不跟着刷新
                Style defaultStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
                if (defaultStyle != null) {
                    StyleConstants.setFontFamily(defaultStyle, newFontName);
                    StyleConstants.setFontSize(defaultStyle, newFontSize);
                }
            }

            // 双重保险：同步更新基础组件状态
            textPane.setFont(new Font(newFontName, Font.PLAIN, newFontSize));
            
            // 🛠️ 关键四：强行让 Swing 的布局管理器抛弃旧缓存，立即重新计算高宽并重绘
            textPane.revalidate();
            textPane.repaint();
        });
    }
}

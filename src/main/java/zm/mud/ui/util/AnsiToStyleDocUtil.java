package zm.mud.ui.util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import zm.mud.ui.theme.ITheme;

@Service
public class AnsiToStyleDocUtil {
    private static final Logger logger = LogManager.getLogger(AnsiToStyleDocUtil.class);
    
    public void parseAnsiToStyledDocument(String text, StyledDocument doc, Font font, ITheme theme,boolean enableBlod) throws BadLocationException {
        if (text == null || text.isEmpty()) return;
        
        SimpleAttributeSet currentAttr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(currentAttr, font.getName());
        StyleConstants.setFontSize(currentAttr, font.getSize());
        
        // 增加两个状态变量，用于记录当前“最原始”的颜色属性，避免 contrast 重复计算导致颜色污染
        Color lastRawFg = theme.getDefaultForeground();
        Color lastRawBg = theme.getDefaultBackground();

        // 首次初始化
        StyleConstants.setForeground(currentAttr, lastRawFg);
        StyleConstants.setBackground(currentAttr, lastRawBg);
        StyleConstants.setBold(currentAttr, false);
        StyleConstants.setUnderline(currentAttr, false);

        int index = 0;
        int len = text.length();

        while (index < len) {
            int nextAnsi = text.indexOf("\u001B[", index);
            
            // 1. 消费普通文本
            if (nextAnsi == -1 || nextAnsi > index) {
                int end = (nextAnsi == -1) ? len : nextAnsi;
                String segment = text.substring(index, end)
                                     .replace("\t", "    ")
                                     .replace("\u3000", "  ");
                if (!segment.isEmpty()) {
                    this.appendString(doc, segment, currentAttr);
                }
                index = end;
                if (index >= len) break;
            }

            // 2. 解析 ANSI 指令
            int mIndex = text.indexOf('m', index);
            if (mIndex > index) {
                String codeStr = text.substring(index + 2, mIndex);
                
                if (codeStr.isEmpty()) { // 处理 \u001B[m 
                    lastRawFg = theme.getDefaultForeground();
                    lastRawBg = theme.getDefaultBackground();
                    resetAttributes(currentAttr, lastRawFg, lastRawBg);
                } else {
                    String[] codes = codeStr.split(";");
                    for (int i = 0; i < codes.length; i++) {
                        String code = codes[i].trim();
                        if (code.isEmpty()) continue;

                        try {
                            switch (code) {
                                case "0":
                                    lastRawFg = theme.getDefaultForeground();
                                    lastRawBg = theme.getDefaultBackground();
                                    resetAttributes(currentAttr, lastRawFg, lastRawBg);
                                    break;
                                case "1":
                                    if(enableBlod){
                                        StyleConstants.setBold(currentAttr, true);
                                    }else{
                                        Color currentFg = StyleConstants.getForeground(currentAttr);
                                        if (currentFg == null) {
                                            currentFg = theme.getDefaultForeground();
                                        }
                                        lastRawFg = theme.toBrighColor(currentFg);
                                    StyleConstants.setForeground(currentAttr, lastRawFg);
                                    }
                                   
                                    break;
                                case "2":
                                    // 弱化当前颜色
                                    Color currentFg = StyleConstants.getForeground(currentAttr);
                                    StyleConstants.setForeground(currentAttr, theme.dimColor(currentFg));
                                    break;
                                case "4":
                                    StyleConstants.setUnderline(currentAttr, true);
                                    break;
                                case "24":
                                    StyleConstants.setUnderline(currentAttr, false);
                                    break;
                                case "38": // 256色扩展前景色
                                    if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                        int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                        lastRawFg = theme.ansi256ToColor(colorIndex);
                                        // 使用具有对比度保障的颜色更新 UI 属性
                                        StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                        i += 2;
                                    }
                                    break;
                                case "48": // 256色扩展背景色
                                    if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                        int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                        lastRawBg = theme.ansi256ToColor(colorIndex);
                                        
                                        StyleConstants.setBackground(currentAttr, lastRawBg);
                                        // 背景变了，前景色基于原始前景重新计算对比度，防止颜色污染
                                        StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                        i += 2;
                                    }
                                    break;
                                default:
                                    // 使用 ITheme 标准方法检测标准颜色代码 (如 30-37, 40-47)
                                    if (theme.isForegroundCode(code)) {
                                        // 优先调用您定义的 default 方法提供扩展性
                                        lastRawFg = theme.resolveForeground(code, lastRawBg); 
                                        StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                    } else if (theme.isBackground(code)) {
                                        lastRawBg = theme.getBackground(code);
                                        StyleConstants.setBackground(currentAttr, lastRawBg);
                                        StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                    } else {
                                        logger.warn("Unknown ANSI code sub-part: " + code + " in sequence: [" + codeStr + "]");
                                    }
                            }
                        } catch (Exception e) {
                            logger.error("Error parsing ANSI code sequence near: " + codeStr, e);
                        }
                    }
                }
                index = mIndex + 1;
            } else {
                // 防御性跳过非标准控制符，避免死循环
                this.appendString(doc, text.substring(index, index + 2), currentAttr);
                index += 2;
            }
        }
    }

    private void resetAttributes(SimpleAttributeSet attr, Color defaultFg, Color defaultBg) {
        StyleConstants.setBold(attr, false);
        StyleConstants.setUnderline(attr, false);
        StyleConstants.setBackground(attr, defaultBg);
        StyleConstants.setForeground(attr, defaultFg);
    }

    private void appendString(StyledDocument doc, String segment, SimpleAttributeSet currentAttr) throws BadLocationException {
        SimpleAttributeSet attrCopy = new SimpleAttributeSet(currentAttr);
        doc.insertString(doc.getLength(), segment, attrCopy);
    }
}

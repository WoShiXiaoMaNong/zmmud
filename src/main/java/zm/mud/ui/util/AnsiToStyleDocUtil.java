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
    
    public void parseAnsiToStyledDocument(String text, StyledDocument doc, Font font, ITheme theme, boolean enableBold) throws BadLocationException {
        if (text == null || text.isEmpty()) return;
        
        SimpleAttributeSet currentAttr = new SimpleAttributeSet(); 
        StyleConstants.setFontFamily(currentAttr, font.getFamily());
        StyleConstants.setFontSize(currentAttr, font.getSize());
        
        Color lastRawFg = theme.getDefaultForeground();
        Color lastRawBg = theme.getDefaultBackground();

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
                                     
                // 注意：如果 segment 仅仅是 "\n" 或 "\r\n"，请确保你的 appendString 
                // 不会与其内部的自动换行逻辑冲突，导致双重空行。
                if (!segment.isEmpty()) {
                    this.appendString(doc, segment, currentAttr);
                }
                index = end;
                if (index >= len) break;
            }

            // 2. 精准解析 ANSI 指令边界
            int terminatorIndex = -1;
            char terminatorChar = 0;
            for (int i = nextAnsi + 2; i < len; i++) {
                char c = text.charAt(i);
                // 匹配任何 ANSI 序列的结束标识符 (A-Z, a-z)
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    terminatorIndex = i;
                    terminatorChar = c;
                    break;
                }
            }

            if (terminatorIndex > nextAnsi) {
                String codeStr = text.substring(nextAnsi + 2, terminatorIndex);
                
                if (terminatorChar == 'm') {
                    // 标准 'm' 结尾的颜色逻辑
                    if (codeStr.isEmpty()) { 
                        lastRawFg = theme.getDefaultForeground();
                        lastRawBg = theme.getDefaultBackground();
                        resetAttributes(currentAttr, lastRawFg, lastRawBg);
                    } else {
                        String[] codes = codeStr.split(";");
                        
                        // 优化：如果 codes 里面包含 "0"，先执行重置，防止 "0" 覆盖同组的其他样式
                        for (String code : codes) {
                            if ("0".equals(code.trim())) {
                                lastRawFg = theme.getDefaultForeground();
                                lastRawBg = theme.getDefaultBackground();
                                resetAttributes(currentAttr, lastRawFg, lastRawBg);
                                break;
                            }
                        }

                        for (int i = 0; i < codes.length; i++) {
                            String code = codes[i].trim();
                            if (code.isEmpty() || "0".equals(code)) continue; // 跳过已处理的 0

                            try {
                                switch (code) {
                                    case "1":
                                        if (enableBold) {
                                            StyleConstants.setBold(currentAttr, true);
                                        } else {
                                            Color currentFg = StyleConstants.getForeground(currentAttr);
                                            if (currentFg == null) {
                                                currentFg = theme.getDefaultForeground();
                                            }
                                            lastRawFg = theme.toBrighColor(currentFg);
                                            StyleConstants.setForeground(currentAttr, lastRawFg);
                                        }
                                        break;
                                    case "2":
                                        Color currentFg = StyleConstants.getForeground(currentAttr);
                                        StyleConstants.setForeground(currentAttr, theme.dimColor(currentFg));
                                        break;
                                    case "4":
                                        StyleConstants.setUnderline(currentAttr, true);
                                        break;
                                    case "24":
                                        StyleConstants.setUnderline(currentAttr, false);
                                        break;
                                    case "38": 
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            lastRawFg = theme.ansi256ToColor(colorIndex);
                                            StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                            i += 2;
                                        }
                                        break;
                                    case "48": 
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            lastRawBg = theme.ansi256ToColor(colorIndex);
                                            StyleConstants.setBackground(currentAttr, lastRawBg);
                                            StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                            i += 2;
                                        }
                                        break;
                                    default:
                                        if (theme.isForegroundCode(code)) {
                                            lastRawFg = theme.resolveForeground(code, lastRawBg); 
                                            StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                        } else if (theme.isBackground(code)) {
                                            lastRawBg = theme.getBackground(code);
                                            StyleConstants.setBackground(currentAttr, lastRawBg);
                                            StyleConstants.setForeground(currentAttr, theme.ensureContrast(lastRawFg, lastRawBg));
                                        }
                                }
                            } catch (Exception e) {
                                logger.error("解析 ANSI 错误: " + code, e);
                            }
                        }
                    }
                }
                index = terminatorIndex + 1;
            } else {
                // 没找到合法的结束符，跳过当前 ESC
                index = nextAnsi + 1;
            }
        }
    }

    private void resetAttributes(SimpleAttributeSet attr, Color fg, Color bg) {
        StyleConstants.setForeground(attr, fg);
        StyleConstants.setBackground(attr, bg);
        StyleConstants.setBold(attr, false);
        StyleConstants.setUnderline(attr, false);
    }

    // 模拟缺失的方法定义
    private void appendString(StyledDocument doc, String text, SimpleAttributeSet attr) throws BadLocationException {
        doc.insertString(doc.getLength(), text, attr);
    }
}

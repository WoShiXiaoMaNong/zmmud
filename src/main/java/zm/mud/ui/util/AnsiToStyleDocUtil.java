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
    
  
    public void parseAnsiToStyledDocument(String text, StyledDocument doc, Font font,ITheme theme) throws BadLocationException {
        SimpleAttributeSet currentAttr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(currentAttr, font.getName());
        StyleConstants.setFontSize(currentAttr, font.getSize());
        StyleConstants.setBackground(currentAttr, theme.getDefaultBackground()); // 默认背景
        StyleConstants.setForeground(currentAttr,theme.getDefaultForeground());
        StyleConstants.setBold(currentAttr, false);
        StyleConstants.setUnderline(currentAttr, false);

        int index = 0;

        while (index < text.length()) {
            if (text.charAt(index) == 0x1B && index + 1 < text.length() && text.charAt(index + 1) == '[') {
                int mIndex = text.indexOf('m', index);
                if (mIndex > index) {
                    String codeStr = text.substring(index + 2, mIndex);
                    String[] codes = codeStr.split(";");

                    for (int i = 0; i < codes.length; i++) {
                        String code = codes[i].trim();
                        switch (code) {
                            case "0":
                                StyleConstants.setBold(currentAttr, false);
                                StyleConstants.setUnderline(currentAttr, false);
                                StyleConstants.setBackground(currentAttr, theme.getDefaultBackground()); // 默认背景
                                StyleConstants.setForeground(currentAttr,theme.getDefaultForeground());
                                break;
                            case "1":
                                StyleConstants.setBold(currentAttr, true);
                                break;
                            case "2":
                                StyleConstants.setForeground(currentAttr,
                                        theme.dimColor(StyleConstants.getForeground(currentAttr)));
                                break;
                            case "4":
                                StyleConstants.setUnderline(currentAttr, true);
                                break;
                            case "24":
                                StyleConstants.setUnderline(currentAttr, false);
                                break;
                            case "38": // 256-color foreground
                                if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                    int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                    StyleConstants.setForeground(currentAttr, theme.ansi256ToColor(colorIndex));
                                    i += 2; // skip next two codes
                                }
                                break;
                            case "48": // 256-color background
                                if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                    int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                    StyleConstants.setBackground(currentAttr, theme.ansi256ToColor(colorIndex));
                                    i += 2;
                                }
                                break;
                            default:
                                if (theme.isForegroundCode(code)) {
                                    Color fg = theme.getForeground(code);
                                    StyleConstants.setForeground(currentAttr, fg);

                                    Color bg = StyleConstants.getBackground(currentAttr);
                                    if (bg != null) {
                                        fg = theme.ensureContrast(fg, bg);
                                        StyleConstants.setForeground(currentAttr, fg);
                                    }
                                } else if (theme.isBackground(code)) {
                                    Color bg = theme.getBackground(code);
                                    Color fg = theme.ensureContrast(StyleConstants.getForeground(currentAttr), bg);
                                    StyleConstants.setForeground(currentAttr, fg);
                                    StyleConstants.setBackground(currentAttr, bg);
                                } else {
                                    logger.error("Unknown ANSI code: " + codeStr);
                                }
                        }
                    }

                    index = mIndex + 1;

                    // 找下一个 ANSI 或文本结尾
                    int next = text.indexOf("\u001B[", index);
                    if (next == -1)
                        next = text.length();
                    String segment = text.substring(index, next);
                    segment = segment.replace("\t", "    ")
                    .replace('\u3000', ' ');
                    // 使用 currentAttr 插入文本
                     this.appendString(doc, segment, currentAttr);
                    index = next;
                } else {
                    index++;
                }
            } else {
                // 普通字符
                int next = text.indexOf("\u001B[", index);
                if (next == -1)
                    next = text.length();
                this.appendString(doc, text.substring(index, next), currentAttr);
                index = next;
            }
        }
    }

    private void appendString(StyledDocument doc,String segment,SimpleAttributeSet currentAttr) throws BadLocationException{
        SimpleAttributeSet attrCopy = new SimpleAttributeSet(currentAttr);
        doc.insertString(doc.getLength(), segment, attrCopy);
    }
 
   
}

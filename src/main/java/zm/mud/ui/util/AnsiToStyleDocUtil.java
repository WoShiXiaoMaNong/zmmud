package zm.mud.ui.util;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AnsiToStyleDocUtil {
    private static final Logger logger = LogManager.getLogger(AnsiToStyleDocUtil.class);
    public static final Map<String, Color> ANSI_FOREGROUND_MAP = Map.ofEntries(
            // 普通前景色
            Map.entry("30", new Color(0, 0, 0)), // 黑
            Map.entry("31", new Color(205, 0, 0)), // 红
            Map.entry("32", new Color(0, 205, 0)), // 绿
            Map.entry("33", new Color(205, 205, 0)), // 黄
            Map.entry("34", new Color(0, 0, 238)), // 蓝
            Map.entry("35", new Color(205, 0, 205)), // 品红 / 紫
            Map.entry("36", new Color(0, 205, 205)), // 青
            // Map.entry("37", new Color(229, 229, 229)), // 白（浅灰）
            Map.entry("37", new Color(117, 117, 117)), // 白（浅灰）

            // 高亮前景色 (Bright)
            Map.entry("90", new Color(127, 127, 127)), // 亮黑 / 深灰
            Map.entry("91", new Color(255, 0, 0)), // 亮红
            Map.entry("92", new Color(0, 255, 0)), // 亮绿
            Map.entry("93", new Color(255, 255, 0)), // 亮黄
            Map.entry("94", new Color(92, 92, 255)), // 亮蓝
            Map.entry("95", new Color(255, 0, 255)), // 亮品红
            Map.entry("96", new Color(0, 255, 255)), // 亮青
            Map.entry("97", new Color(255, 255, 255)) // 亮白
    );

    public static final Map<String, Color> ANSI_BACKGROUND_MAP = Map.ofEntries(
            Map.entry("40", new Color(0, 0, 0)), // 黑
            Map.entry("41", new Color(205, 0, 0)), // 红
            Map.entry("42", new Color(0, 205, 0)), // 绿
            Map.entry("43", new Color(205, 205, 0)), // 黄
            Map.entry("44", new Color(0, 0, 238)), // 蓝
            Map.entry("45", new Color(205, 0, 205)), // 品红 / 紫
            Map.entry("46", new Color(0, 205, 205)), // 青
            Map.entry("47", new Color(229, 229, 229)), // 白（浅灰）
            Map.entry("100", new Color(127, 127, 127)), // 高亮黑 / 深灰
            Map.entry("101", new Color(255, 0, 0)), // 高亮红
            Map.entry("102", new Color(0, 255, 0)), // 高亮绿
            Map.entry("103", new Color(255, 255, 0)), // 高亮黄
            Map.entry("104", new Color(92, 92, 255)), // 高亮蓝
            Map.entry("105", new Color(255, 0, 255)), // 高亮品红
            Map.entry("106", new Color(0, 255, 255)), // 高亮青
            Map.entry("107", new Color(255, 255, 255)) // 高亮白
    );

    public void parseAnsiToStyledDocument(String text, StyledDocument doc,Font font) throws BadLocationException {
        SimpleAttributeSet currentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(currentAttr, Color.BLACK);
        StyleConstants.setFontFamily(currentAttr, font.getName()); 
        StyleConstants.setFontSize(currentAttr, font.getSize());
        StyleConstants.setBackground(currentAttr, Color.WHITE); // 默认背景
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
                                StyleConstants.setForeground(currentAttr, Color.BLACK);
                                StyleConstants.setBackground(currentAttr, Color.WHITE);
                                break;
                            case "1":
                                StyleConstants.setBold(currentAttr, true);
                                break;
                            case "2":
                                StyleConstants.setForeground(currentAttr, dimColor(StyleConstants.getForeground(currentAttr)));
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
                                    StyleConstants.setForeground(currentAttr, ansi256ToColor(colorIndex));
                                    i += 2; // skip next two codes
                                }
                                break;
                            case "48": // 256-color background
                                if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                    int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                    StyleConstants.setBackground(currentAttr, ansi256ToColor(colorIndex));
                                    i += 2;
                                }
                                break;
                            default:
                                if (ANSI_FOREGROUND_MAP.containsKey(code)) {
                                    StyleConstants.setForeground(currentAttr, ANSI_FOREGROUND_MAP.get(code));
                                } else if (ANSI_BACKGROUND_MAP.containsKey(code)) {
                                    StyleConstants.setBackground(currentAttr, ANSI_BACKGROUND_MAP.get(code));
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

                    // 使用 currentAttr 插入文本
                    doc.insertString(doc.getLength(), segment, currentAttr);

                    index = next;
                } else {
                    index++;
                }
            } else {
                // 普通字符
                int next = text.indexOf("\u001B[", index);
                if (next == -1)
                    next = text.length();
                doc.insertString(doc.getLength(), text.substring(index, next), currentAttr);
                index = next;
            }
        }
    }

    private Color dimColor(Color c) {
    int brightness = (int)(0.299*c.getRed() + 0.587*c.getGreen() + 0.114*c.getBlue());
    // 如果太亮，就不调暗
    if (brightness > 200) return c;
    int r = (int)(c.getRed() * 0.7);
    int g = (int)(c.getGreen() * 0.7);
    int b = (int)(c.getBlue() * 0.7);
    return new Color(r, g, b);
}

    private boolean isLightColor(Color color) {
        // 亮度公式：0.299*R + 0.587*G + 0.114*B
        int brightness = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        return brightness > 200; // 可调阈值
    }

    private Color ansi256ToColor(int index) {
    if (index < 16) {
        // Standard + bright colors (reuse your maps)
        return switch (index) {
            case 0 -> new Color(0,0,0);
            case 1 -> new Color(205,0,0);
            case 2 -> new Color(0,205,0);
            case 3 -> new Color(205,205,0);
            case 4 -> new Color(0,0,238);
            case 5 -> new Color(205,0,205);
            case 6 -> new Color(0,205,205);
            case 7 -> new Color(229,229,229);
            case 8 -> new Color(127,127,127);
            case 9 -> new Color(255,0,0);
            case 10 -> new Color(0,255,0);
            case 11 -> new Color(255,255,0);
            case 12 -> new Color(92,92,255);
            case 13 -> new Color(255,0,255);
            case 14 -> new Color(0,255,255);
            case 15 -> new Color(255,255,255);
            default -> Color.BLACK;
        };
    } else if (index >= 16 && index <= 231) {
        // 6x6x6 color cube
        int idx = index - 16;
        int r = (idx / 36) % 6;
        int g = (idx / 6) % 6;
        int b = idx % 6;
        return new Color(r == 0 ? 0 : 55 + r * 40,
                         g == 0 ? 0 : 55 + g * 40,
                         b == 0 ? 0 : 55 + b * 40);
    } else if (index >= 232 && index <= 255) {
        // grayscale
        int gray = 8 + (index - 232) * 10;
        return new Color(gray, gray, gray);
    }
    return Color.BLACK; // fallback
}
}

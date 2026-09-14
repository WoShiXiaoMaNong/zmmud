package zm.mud.core.text.ansi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import zm.mud.core.text.ZmmudText;
import zm.mud.core.text.TextToken;
import zm.mud.ui.theme.ITheme;

@Service
public class AnsiToTokenUtil {
    private static final Logger logger = LogManager.getLogger(AnsiToTokenUtil.class);

    public ZmmudText parseAnsiToTokens(String text, ITheme theme, boolean enableBold) {
        
        List<TextToken> tokens = new ArrayList<>();
        ZmmudText ansiText = new ZmmudText(text, tokens);
        if (text == null || text.isEmpty()) return ansiText;

        // 1. 记录原始的 ANSI 编码字符串
        String lastRawFgCode = "default";
        String lastRawBgCode = "default";

        // 2. 记录theme渲染过的颜色计算
        Color currentRenderedFg = theme.getDefaultForeground();
        Color currentRenderedBg = theme.getDefaultBackground();

        boolean isBold = false;
        boolean isUnderline = false;

        int index = 0;
        int len = text.length();

        while (index < len) {
            int nextAnsi = text.indexOf("\u001B[", index);

            // 消费普通文本
            if (nextAnsi == -1 || nextAnsi > index) {
                int end = (nextAnsi == -1) ? len : nextAnsi;
                String segment = text.substring(index, end)
                                     .replace("\t", "    ")
                                     .replace("\u3000", "  ");

                if (!segment.isEmpty()) {
                    // 塞入 Token，raw 字段保留原始 ANSI 字符串，render 字段保留最终计算的 RGB
                    tokens.add(new TextToken(
                            segment,
                            lastRawFgCode,
                            currentRenderedFg.getRGB() & 0xFFFFFF,
                            lastRawBgCode,
                            currentRenderedBg.getRGB() & 0xFFFFFF,
                            isBold,
                            isUnderline
                    ));
                }
                index = end;
                if (index >= len) break;
            }

            // 精准解析 ANSI 指令边界
            int terminatorIndex = -1;
            char terminatorChar = 0;
            for (int i = nextAnsi + 2; i < len; i++) {
                char c = text.charAt(i);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    terminatorIndex = i;
                    terminatorChar = c;
                    break;
                }
            }

            if (terminatorIndex > nextAnsi) {
                String codeStr = text.substring(nextAnsi + 2, terminatorIndex);

                if (terminatorChar == 'm') {
                    if (codeStr.isEmpty()) {
                        // 重置
                        lastRawFgCode = "default";
                        lastRawBgCode = "default";
                        currentRenderedFg = theme.getDefaultForeground();
                        currentRenderedBg = theme.getDefaultBackground();
                        isBold = false;
                        isUnderline = false;
                    } else {
                        String[] codes = codeStr.split(";");

                        for (String code : codes) {
                            if ("0".equals(code.trim())) {
                                lastRawFgCode = "default";
                                lastRawBgCode = "default";
                                currentRenderedFg = theme.getDefaultForeground();
                                currentRenderedBg = theme.getDefaultBackground();
                                isBold = false;
                                isUnderline = false;
                                break;
                            }
                        }

                        for (int i = 0; i < codes.length; i++) {
                            String code = codes[i].trim();
                            if (code.isEmpty() || "0".equals(code)) continue;

                            try {
                                switch (code) {
                                    case "1":
                                        if (enableBold) {
                                            isBold = true;
                                        } else {
                                            currentRenderedFg = theme.toBrighColor(currentRenderedFg);
                                        }
                                        break;
                                    case "2":
                                        currentRenderedFg = theme.dimColor(currentRenderedFg);
                                        break;
                                    case "4":
                                        isUnderline = true;
                                        break;
                                    case "24":
                                        isUnderline = false;
                                        break;
                                    case "38":
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            
                                            // 直接拼装记录原始 256色 ANSI 字符串，例如 "38;5;123"
                                            lastRawFgCode = "38;5;" + colorIndex;
                                            
                                            Color rawFgColor = theme.ansi256ToColor(colorIndex);
                                            currentRenderedFg = theme.ensureContrast(rawFgColor, currentRenderedBg);
                                            i += 2;
                                        }
                                        break;
                                    case "48":
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            
                                            // 【直接拼装记录原始 256色背景 ANSI 字符串，例如 "48;5;123"
                                            lastRawBgCode = "48;5;" + colorIndex;
                                            
                                            Color rawBgColor = theme.ansi256ToColor(colorIndex);
                                            currentRenderedBg = rawBgColor;
                                            currentRenderedFg = theme.ensureContrast(currentRenderedFg, currentRenderedBg);
                                            i += 2;
                                        }
                                        break;
                                    default:
                                        if (theme.isForegroundCode(code)) {
                                            // 直接记录原始基本前景 ANSI 码，如 "31"
                                            lastRawFgCode = code;
                                            
                                            Color rawFgColor = theme.resolveForeground(code, currentRenderedBg);
                                            currentRenderedFg = theme.ensureContrast(rawFgColor, currentRenderedBg);
                                        } else if (theme.isBackground(code)) {
                                            // 直接记录原始基本背景 ANSI 码，如 "42"
                                            lastRawBgCode = code;
                                            
                                            currentRenderedBg = theme.getBackground(code);
                                            currentRenderedFg = theme.ensureContrast(currentRenderedFg, currentRenderedBg);
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
                index = nextAnsi + 1;
            }
        }
        return ansiText;
    }
}

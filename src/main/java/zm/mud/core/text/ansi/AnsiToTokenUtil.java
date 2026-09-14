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

    /**
     * 兼容单行/单次调用方法（每次使用全新的默认上下文）
     */
    public ZmmudText parseAnsiToTokens(String text, ITheme theme, boolean enableBold) {
        return parseAnsiToTokens(text, theme, enableBold, new AnsiContext(theme));
    }

    /**
     * 核心多行/流式解析方法：传入持久化的 AnsiContext 跨行维持颜色
     */
    public ZmmudText parseAnsiToTokens(String text, ITheme theme, boolean enableBold, AnsiContext ctx) {
        List<TextToken> tokens = new ArrayList<>();
        ZmmudText ansiText = new ZmmudText(text, tokens);
        if (text == null || text.isEmpty()) return ansiText;

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
                    // 使用上下文中的持久颜色状态
                    tokens.add(new TextToken(
                            segment,
                            ctx.getLastRawFgCode(),
                            ctx.getCurrentRenderedFg().getRGB() & 0xFFFFFF,
                            ctx.getLastRawBgCode(),
                            ctx.getCurrentRenderedBg().getRGB() & 0xFFFFFF,
                            ctx.isBold(),
                            ctx.isUnderline()
                    ));
                }
                index = end;
                if (index >= len) break;
            }

            // 2. 精准解析 ANSI 指令边界
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
                        // 空参数重置：\u001B[m
                        ctx.reset(theme);
                    } else {
                        String[] codes = codeStr.split(";");

                        // 优先检查是否有全局重置码 0
                        for (String code : codes) {
                            if ("0".equals(code.trim())) {
                                ctx.reset(theme);
                                break;
                            }
                        }

                        // 逐个解析样式码
                        for (int i = 0; i < codes.length; i++) {
                            String code = codes[i].trim();
                            if (code.isEmpty() || "0".equals(code)) continue;

                            // 优先检测是否属于 16 色标准前景色 (直接通过 theme 接口判断)
                            if (theme.isForegroundCode(code)) {
                                ctx.setLastRawFgCode(code);
                                Color rawFg = theme.getForeground(code);
                                ctx.setCurrentRenderedFg(theme.ensureContrast(rawFg, ctx.getCurrentRenderedBg()));
                                continue;
                            }

                            // 优先检测是否属于 16 色标准背景色 (直接通过 theme 接口判断)
                            if (theme.isBackground(code)) {
                                ctx.setLastRawBgCode(code);
                                Color rawBg = theme.getBackground(code);
                                ctx.setCurrentRenderedBg(rawBg);
                                // 当背景色改变时，重新校验当前前景色的对比度
                                ctx.setCurrentRenderedFg(theme.ensureContrast(ctx.getOriginalFgColor(), rawBg));
                                continue;
                            }

                            try {
                                switch (code) {
                                    case "1":
                                        if (enableBold) {
                                            ctx.setBold(true);
                                        } else {
                                            ctx.setCurrentRenderedFg(theme.toBrighColor(ctx.getCurrentRenderedFg()));
                                        }
                                        break;
                                    case "2":
                                        ctx.setCurrentRenderedFg(theme.dimColor(ctx.getCurrentRenderedFg()));
                                        break;
                                    case "4":
                                        ctx.setUnderline(true);
                                        break;
                                    case "24":
                                        ctx.setUnderline(false);
                                        break;
                                    case "38": // 256色前景色
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            ctx.setLastRawFgCode("38;5;" + colorIndex);
                                            Color rawFgColor = theme.ansi256ToColor(colorIndex);
                                            ctx.setCurrentRenderedFg(theme.ensureContrast(rawFgColor, ctx.getCurrentRenderedBg()));
                                            i += 2;
                                        }
                                        break;
                                    case "48": // 256色背景色
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            ctx.setLastRawBgCode("48;5;" + colorIndex);
                                            Color rawBgColor = theme.ansi256ToColor(colorIndex);
                                            ctx.setCurrentRenderedBg(rawBgColor);
                                            ctx.setCurrentRenderedFg(theme.ensureContrast(ctx.getOriginalFgColor(), rawBgColor));
                                            i += 2;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } catch (Exception e) {
                                logger.error("解析单个 ANSI 编码失败: " + code, e);
                            }
                        }
                    }
                }
                index = terminatorIndex + 1;
            } else {
                index = nextAnsi + 2; // 降级，防止死循环
            }
        }
        return ansiText;
    }
}

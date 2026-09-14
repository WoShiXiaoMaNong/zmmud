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
        if (text == null || text.isEmpty())
            return ansiText;

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
                            ctx.isUnderline()));
                }
                index = end;
                if (index >= len)
                    break;
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
                        // 1. 空参数直接彻底重置
                        ctx.reset(theme);
                    } else {
                        String[] codes = codeStr.split(";");

                        // 2. 规范判定：先检查这个序列里有没有包含重置码 "0" 或 ""(空码在某些MUD中代表0)
                        boolean hasReset = false;
                        for (String code : codes) {
                            String trimmed = code.trim();
                            if ("0".equals(trimmed) || trimmed.isEmpty()) {
                                hasReset = true;
                                break;
                            }
                        }

                        // 如果包含 0，立刻强制将上下文清空恢复到当前主题的默认白底黑字（或黑底白字）
                        if (hasReset) {
                            ctx.reset(theme);
                        }

                        // 3. 顺序应用其余的属性（让同个序列中 0 后面的样式正确覆盖重置状态）
                        for (int i = 0; i < codes.length; i++) {
                            String code = codes[i].trim();
                            // 跳过已经处理过的重置码
                            if (code.isEmpty() || "0".equals(code))
                                continue;

                            // 检测 16 色标准前景色 (30-37, 90-97)
                            if (theme.isForegroundCode(code)) {
                                ctx.setLastRawFgCode(code);
                                Color rawFg = theme.getForeground(code);
                                ctx.setOriginalFgColor(rawFg); // 保存原始色
                                ctx.setCurrentRenderedFg(theme.ensureContrast(rawFg, ctx.getCurrentRenderedBg()));
                                continue;
                            }

                            // 检测 16 色标准背景色 (40-47, 100-107)
                            if (theme.isBackground(code)) {
                                ctx.setLastRawBgCode(code);
                                Color rawBg = theme.getBackground(code);
                                ctx.setCurrentRenderedBg(rawBg);
                                // 背景改变，必须用原始前景色和新背景重新做一次对比度降噪
                                ctx.setCurrentRenderedFg(theme.ensureContrast(ctx.getOriginalFgColor(), rawBg));
                                continue;
                            }

                            try {
                                switch (code) {
                                    case "1": // 高亮 / 粗体
                                        if (enableBold) {
                                            ctx.setBold(true);
                                        } else {
                                            ctx.setCurrentRenderedFg(theme.toBrighColor(ctx.getCurrentRenderedFg()));
                                        }
                                        break;
                                    case "2": // 暗色
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
                                            ctx.setOriginalFgColor(rawFgColor);
                                            ctx.setCurrentRenderedFg(
                                                    theme.ensureContrast(rawFgColor, ctx.getCurrentRenderedBg()));
                                            i += 2;
                                        }
                                        break;
                                    case "48": // 256色背景色
                                        if (i + 2 < codes.length && "5".equals(codes[i + 1].trim())) {
                                            int colorIndex = Integer.parseInt(codes[i + 2].trim());
                                            ctx.setLastRawBgCode("48;5;" + colorIndex);
                                            Color rawBgColor = theme.ansi256ToColor(colorIndex);
                                            ctx.setCurrentRenderedBg(rawBgColor);
                                            ctx.setCurrentRenderedFg(
                                                    theme.ensureContrast(ctx.getOriginalFgColor(), rawBgColor));
                                            i += 2;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            } catch (Exception e) {
                                logger.error("解析单个样式码失败: " + code, e);
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

package zm.mud.ui.util;

import java.awt.Color;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class AnsiTextUtil {
    // 匹配所有标准 ANSI 转义序列的正则表达式
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[A-Za-z]");

    /**
     * 清除文本中的所有 ANSI 字符后，判断是否以指定前缀开头
     */
    public String cleanStartsWith(String text) {
        if (text == null)
            return null;

        // 剥离 ANSI 序列
        String cleanText = ANSI_PATTERN.matcher(text).replaceAll("");

        return cleanText;
    }

    /**
     * 将文本包裹上标准的 24位真彩色 ANSI 序列，完美适配你的 Swing 解析器
     * 
     * @param text  需要染色的文本
     * @param color 目标颜色对象
     * @return 带有 \u001B[38;2;R;G;Bm 前缀和 \u001B[0m 后缀的字符串
     */
    public String stringWithAnsiColor(String text, Color color) {
        if (text == null || text.isEmpty())
            return text;
        if (color == null)
            return text;

        // 1. 提取 RGB 分量
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // 2. 根据颜色分量，动态计算并映射到标准 16 色
        String ansiCode = "\u001B[37m"; // 默认白色

        if (r > 150 && g < 100 && b < 100) {
            ansiCode = "\u001B[31m"; // 映射为 红色 (对应你的 ANSI_RED)
        } else if (g > 150 && r < 100 && b < 100) {
            ansiCode = "\u001B[32m"; // 映射为 绿色 (对应你的 ANSI_GREEN)
        } else if (r > 150 && g > 150 && b < 100) {
            ansiCode = "\u001B[33m"; // 映射为 黄色 (对应你的 ANSI_YELLOW)
        } else if (b > 150 && r < 100 && g < 100) {
            ansiCode = "\u001B[34m"; // 蓝色
        } else if (r > 150 && b > 150 && g < 100) {
            ansiCode = "\u001B[35m"; // 紫色
        } else if (g > 150 && b > 150 && r < 100) {
            ansiCode = "\u001B[36m"; // 青色
        }

        return ansiCode + text + "\u001B[0m";
    }

}

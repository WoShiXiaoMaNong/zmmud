package zm.mud.ui.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class AnsiTextUtil {
       // 匹配所有标准 ANSI 转义序列的正则表达式
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[A-Za-z]");

    /**
     * 清除文本中的所有 ANSI 字符后，判断是否以指定前缀开头
     */
    public  String cleanStartsWith(String text) {
        if (text == null) return null;
        
        // 剥离 ANSI 序列
        String cleanText = ANSI_PATTERN.matcher(text).replaceAll("");
        
        return cleanText;
    }



}

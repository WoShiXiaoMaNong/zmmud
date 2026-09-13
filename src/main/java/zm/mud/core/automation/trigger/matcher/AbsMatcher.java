package zm.mud.core.automation.trigger.matcher;

import java.util.regex.Pattern;

import zm.mud.core.automation.trigger.cfg.MatchResult;

public abstract class AbsMatcher implements IMatcher {
    // 匹配所有标准 ANSI 转义序列的正则表达式
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*[A-Za-z]");

    private String expression;
    private Boolean matchRawMsg;

    @Override
    public final MatchResult match(String msg) {

        if (Boolean.TRUE.equals(this.getMatchRawMsg())) {
            return this.doMatch(msg);
        } else {
            return this.doMatch(this.cleanStartsWith(msg));
        }

    }



    protected abstract MatchResult doMatch(String msg);

    public Boolean getMatchRawMsg() {
        return matchRawMsg;
    }

    public void setMatchRawMsg(Boolean matchRawMsg) {
        this.matchRawMsg = matchRawMsg;
    }

    @Override
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

        /*
     * 清除文本中的所有 ANSI 字符后，判断是否以指定前缀开头
     */
    private String cleanStartsWith(String text) {
        if (text == null)
            return null;

        // 剥离 ANSI 序列
        String cleanText = ANSI_PATTERN.matcher(text).replaceAll("");

        return cleanText;
    }

}

package zm.mud.core.trigger.matcher;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import zm.mud.core.trigger.cfg.MatchResult;

@Component("MATCHER_Regex")
@Scope("prototype")
public class RegexMatcher implements IMatcher {

    private String expression;
    private Pattern pattern;

    @Override
    public MatchResult match(String msg) {
        if (msg == null || this.pattern == null) {
            return MatchResult.UNMATCHED(msg);
        }

        Matcher matcher = this.pattern.matcher(msg);

        if (matcher.find()) {
            List<String> matchedRet = new ArrayList<>();
            
            // matcher.groupCount() 返回的是括号捕获组的数量
            // i = 0 是整个正则匹配到的完整文本，i >= 1 是各个括号内的捕获内容
            for (int i = 0; i <= matcher.groupCount(); i++) {
                matchedRet.add(matcher.group(i));
            }
            
            return MatchResult.MATCHED(msg, matchedRet);
        } else {
            return MatchResult.UNMATCHED(msg);
        }
    }

    @Override
    public void setExpression(String expression) {
        this.expression = expression;
        if (expression != null) {
            this.pattern = Pattern.compile(expression);
        } else {
            this.pattern = null;
        }
    }

    @Override
    public String getExpression() {
        return this.expression;
    }
}

package zm.mud.core.text;

import java.util.List;

public class ZmmudText {
    private String originText;
    private List<TextToken> textTokens;

    public ZmmudText(String originText, List<TextToken> textTokens) {
        this.originText = originText;
        this.textTokens = textTokens;
    }
    public String getOriginText() {
        return originText;
    }
    public List<TextToken> getTextTokens() {
        return textTokens;
    }
   
    
}

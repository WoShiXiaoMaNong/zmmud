package zm.mud.ui.processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.util.AnsiTextUtil;

@Component
@Scope("prototype")
public class MsgPrintProcessor implements Function<InbMsg,Boolean>{

    @Autowired
    private ZmMudUI ui;


    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Pattern CHAT_MSG_HEADER_PATTERN = 
            Pattern.compile("^(【[^】]+】)(.*)$");
            
    @Autowired
    private AnsiTextUtil ansiTextUtil;
    
    @Value("${mud.ui.showTimestamp:false}")
    private boolean showTimestamp;

    @Override
    public Boolean apply(InbMsg t) {
        if( isAnsiEmpty(t.getContent())){
            return true;
        }
        if(isChatMsg(t)){
            ui.printlnToScreen(t.getSession(),this.getMsgStr(t),true);
        }else{
            ui.printlnToScreen(t.getSession(),this.getMsgStr(t));
        }
        
        return true;
    }
      private String getMsgStr(InbMsg msg) {
        if (showTimestamp && msg.getTimestamp() != null) {
            LocalDateTime time = msg.getTimestamp();

            return "[" + TIME_FMT.format(time) + "] " + msg.getContent();
        } else {
            return msg.getContent();
        }
    }

    private boolean isAnsiEmpty(String text) {
        if (text == null) return true;
        
        // 正则表达式匹配标准的 ANSI 转义序列 (例如 \u001B[2;37;0m 或 \u001B[m)
        // \u001B 是 ESC 键，后面跟随 [，然后是任意数量的数字/分号，最后以 A-Z 或 a-z 结尾
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*[A-Za-z]", "");
        
        // 剔除 ANSI 码后，再剔除前后空格（包括全角空格 \u3000）
        cleanText = cleanText.replace("\u3000", "").trim();
        
        // 如果最后什么都不剩，说明是个纯属性控制空行
        return cleanText.isEmpty();
    }
    private boolean isChatMsg(InbMsg msg){
        if (msg == null){
            return false;
        }
        String msgStr = msg.getContent();
        String cleanStr = this.ansiTextUtil.cleanStartsWith(msgStr);
        if( !cleanStr.startsWith("【")){
            return false;
        }
        
        boolean isChatMsg = false;
        Matcher matcher = CHAT_MSG_HEADER_PATTERN.matcher(cleanStr);
        if (matcher.matches()) { // 使用 matches 全文匹配或者 find 都可以，因为加了 ^ 锚定行首
            isChatMsg = true;
            
        } else {
            isChatMsg = false;
        }
        return isChatMsg;
    }
}

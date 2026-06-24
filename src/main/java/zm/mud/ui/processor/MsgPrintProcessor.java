package zm.mud.ui.processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.util.AnsiTextUtil;

@Component
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
        if(isChatMsg(t)){
            ui.printlnToScreen(this.getMsgStr(t),true);
        }else{
            ui.printlnToScreen(this.getMsgStr(t));
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

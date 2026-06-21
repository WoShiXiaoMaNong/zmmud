package zm.mud.ui.processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import zm.mud.network.inbound.message.InbMsg;
import zm.mud.ui.ZmMudUI;

@Component
public class MsgPrintProcessor implements Function<InbMsg,Boolean>{

    @Autowired
    private ZmMudUI ui;


    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

 
    @Value("${mud.ui.showTimestamp:false}")
    private boolean showTimestamp;

    @Override
    public Boolean apply(InbMsg t) {
          ui.printlnToScreen(this.getMsgStr(t));
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
}

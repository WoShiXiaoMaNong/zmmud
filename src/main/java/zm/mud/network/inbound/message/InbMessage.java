package zm.mud.network.inbound.message;

import java.time.LocalDateTime;

public interface InbMessage {
   
    String getContent();

    LocalDateTime getTimestamp();


    public static IACConfirmInbMsg buildIACConfirmMsg(int[] content) {
        return new IACConfirmInbMsg(content);
    }

    public static InbMessage build(String content) {
        return new NormalInbMsg(content);
    }
}

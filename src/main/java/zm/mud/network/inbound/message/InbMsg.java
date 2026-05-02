package zm.mud.network.inbound.message;

import java.time.LocalDateTime;

public interface InbMsg {
   
    String getContent();

    LocalDateTime getTimestamp();


    public static IACConfirmInbMsg buildIACConfirmMsg(int[] content) {
        return new IACConfirmInbMsg(content);
    }

    public static InbMsg build(String content) {
        return new NormalInbMsg(content);
    }
}

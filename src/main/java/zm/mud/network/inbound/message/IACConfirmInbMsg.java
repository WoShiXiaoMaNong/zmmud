package zm.mud.network.inbound.message;

import java.time.LocalDateTime;

public class IACConfirmInbMsg implements InbMsg  {
    private int[] content;
    private LocalDateTime timestamp;

    public IACConfirmInbMsg(int[] content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String getContent() {
       return "";
    }

    public int[] getContentBytes() {
        return this.content;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        return "IACConfirmMessage {content=" + java.util.Arrays.toString(this.content) + ", timestamp=" + this.timestamp + "}";
    }

    
}

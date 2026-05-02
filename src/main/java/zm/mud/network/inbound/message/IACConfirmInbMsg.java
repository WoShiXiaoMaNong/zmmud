package zm.mud.network.inbound.message;

import java.time.LocalDateTime;

public class IACConfirmInbMsg implements InbMsg  {
    private byte[] content;
    private LocalDateTime timestamp;

    public IACConfirmInbMsg(byte[] content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String getContent() {
       return "";
    }

    public byte[] getContentBytes() {
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

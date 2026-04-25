package zm.mud.network.inbound.message;

import java.time.LocalDateTime;

public class NormalInbMsg implements InbMessage {
    private String content;
    private LocalDateTime timestamp;

    public NormalInbMsg(String content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String toString() {
        // return "[" + timestamp + "] " + content;
        return content;
    }
}

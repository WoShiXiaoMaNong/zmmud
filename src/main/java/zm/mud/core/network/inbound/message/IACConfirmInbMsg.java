package zm.mud.core.network.inbound.message;

import java.time.LocalDateTime;

import zm.mud.core.session.MudSession;

public class IACConfirmInbMsg implements InbMsg  {
    private byte[] content;
    private LocalDateTime timestamp;

    private MudSession session;

    public IACConfirmInbMsg(MudSession session,byte[] content) {
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
    @Override
    public MudSession getSession() {
        return session;
    }

    @Override
    public void setSession(MudSession session) {
        this.session = session;
    }

    
}

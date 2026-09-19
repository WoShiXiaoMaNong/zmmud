package zm.mud.core.network.inbound.message;

import java.time.LocalDateTime;

import zm.mud.core.session.MudSession;

public class NormalInbMsg implements InbMsg {
    private String content;
    private LocalDateTime timestamp;
    private MudSession session;
    public NormalInbMsg(MudSession session,String content) {
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
    
    @Override
    public void setSession(MudSession session) {
        this.session = session;
    }


    @Override
    public MudSession getSession() {
        return session;
    }

}

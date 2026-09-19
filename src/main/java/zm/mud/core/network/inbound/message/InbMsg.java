package zm.mud.core.network.inbound.message;

import java.time.LocalDateTime;

import zm.mud.core.session.MudSession;

public interface InbMsg {
   
    String getContent();

    LocalDateTime getTimestamp();

    MudSession getSession();

    void setSession(MudSession session);


    public static IACConfirmInbMsg buildIACConfirmMsg(MudSession session,byte[] content) {
        return new IACConfirmInbMsg(session,content);
    }

    public static InbMsg build(MudSession session,String content) {
        return new NormalInbMsg(session,content);
    }
}

package zm.mud.core.network.inbound.message;

import java.time.LocalDateTime;

import zm.mud.core.session.MudSession;

public interface InbMsg {
   
    String getContent();

    LocalDateTime getTimestamp();

    MudSession getSession();

    /**
     * 标记这个消息是否还可用
     * @return
     */
    boolean isConsumable();

    /**
     * 将该消息设置为 不可用
     */
    void setUnconsumable();

    void setSession(MudSession session);


    public static IACConfirmInbMsg buildIACConfirmMsg(MudSession session,byte[] content) {
        return new IACConfirmInbMsg(session,content);
    }

    public static InbMsg build(MudSession session,String content) {
        return new NormalInbMsg(session,content);
    }
}

package zm.mud.core.network.outbound.message;

import zm.mud.core.session.MudSession;

public class NrmOubMsg  implements OubMsg {

    private String content;
    private MudSession session;

    public NrmOubMsg(MudSession session,String content) {
        this.content = content;
        this.session = session;
    }

    public String getContent() {
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

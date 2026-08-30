package zm.mud.core.network.outbound.message;

import zm.mud.core.session.MudSession;

public interface OubMsg {

    String getContent();

    void setSession(MudSession session);

    MudSession getSession();
    
}

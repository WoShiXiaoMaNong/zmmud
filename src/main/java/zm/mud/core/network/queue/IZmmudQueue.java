package zm.mud.core.network.queue;

import zm.mud.core.session.MudSession;

public interface IZmmudQueue<T> {
    void put(MudSession session,T b);
    T take(MudSession session);
}

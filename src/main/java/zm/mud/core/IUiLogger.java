package zm.mud.core;

import zm.mud.core.session.MudSession;

public interface IUiLogger {
    void info(MudSession session, String msg);

    void warn(MudSession session, String msg);

    void error(MudSession session, String msg);

    void print(MudSession session, String msg);
}

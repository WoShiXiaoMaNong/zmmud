package zm.mud.core.api.oub.cmd;

import java.util.Collections;
import java.util.List;

import zm.mud.core.api.oub.IOubCommand;
import zm.mud.core.session.MudSession;

public class NormalOubCommand extends AbsOubCommand{

    public NormalOubCommand(MudSession session, String originCommandStr) {
        super(session, originCommandStr);
    }

    @Override
    protected List<IOubCommand> transferCommand(String originCommandStr) {
        return Collections.singletonList(this);
    } 
}

package zm.mud.core.command.cmd;

import java.util.Collections;
import java.util.List;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.session.MudSession;
import zm.mud.core.utils.DirectionReversionUtil;

public class NormalOubCommand extends AbsOubCommand{

    public NormalOubCommand(MudSession session, String originCommandStr) {
        super(session, originCommandStr);
    }

    @Override
    protected List<IOubCommand> transferCommand(String originCommandStr) {
        IOubCommand cmd = new NormalOubCommand(getSession(),  DirectionReversionUtil.revert(originCommandStr));
        cmd.setOriginalCmd(false);
        cmd.setRevertMode(this.revertMode());
        return Collections.singletonList(cmd);        
    }

     @Override
    public String getCommandStr() {
        if(this.revertMode()){
            return  DirectionReversionUtil.revert(super.getCommandStr());
        }else{
            return super.getCommandStr();
        }
    }

}

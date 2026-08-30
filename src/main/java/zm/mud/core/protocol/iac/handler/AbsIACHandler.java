package zm.mud.core.protocol.iac.handler;

import java.util.List;

import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.session.MudSession;

public abstract class AbsIACHandler implements IIACCommandHandler{

      @Override
    public List<byte[]> handle(MudSession session,byte[] iacCommand) {
        int commandCode = iacCommand[1] & 0XFF;
        List<byte[]> responseMsgs = null;
        switch (commandCode){
            case IACConsts.CMD_WILL -> responseMsgs = this.handle_WILL(session,iacCommand);
            case IACConsts.CMD_WONT -> responseMsgs = this.handle_WONT(session,iacCommand);
            case IACConsts.CMD_DO   -> responseMsgs = this.handle_DO(session,iacCommand);
            case IACConsts.CMD_DONT -> responseMsgs = this.handle_DONT(session,iacCommand);
            default -> responseMsgs = null;
        }

        return responseMsgs;
    }

    
    protected abstract List<byte[]> handle_WILL(MudSession session,byte[] iacCommand);

    protected abstract List<byte[]> handle_WONT(MudSession session,byte[] iacCommand) ;

    protected abstract List<byte[]> handle_DO(MudSession session,byte[] iacCommand);

    protected abstract List<byte[]> handle_DONT(MudSession session,byte[] iacCommand) ;
  
}

package zm.mud.core.protocol.iac.handler;

import java.util.List;

import zm.mud.core.protocol.iac.consts.IACConsts;

public abstract class AbsIACHandler implements IIACCommandHandler{

      @Override
    public List<byte[]> handle(byte[] iacCommand) {
        int commandCode = iacCommand[1] & 0XFF;
        List<byte[]> responseMsgs = null;
        switch (commandCode){
            case IACConsts.CMD_WILL -> responseMsgs = this.handle_WILL(iacCommand);
            case IACConsts.CMD_WONT -> responseMsgs = this.handle_WONT(iacCommand);
            case IACConsts.CMD_DO   -> responseMsgs = this.handle_DO(iacCommand);
            case IACConsts.CMD_DONT -> responseMsgs = this.handle_DONT(iacCommand);
            default -> responseMsgs = null;
        }

        return responseMsgs;
    }

    
    protected abstract List<byte[]> handle_WILL(byte[] iacCommand);

    protected abstract List<byte[]> handle_WONT(byte[] iacCommand) ;

    protected abstract List<byte[]> handle_DO(byte[] iacCommand);

    protected abstract List<byte[]> handle_DONT(byte[] iacCommand) ;
  
}

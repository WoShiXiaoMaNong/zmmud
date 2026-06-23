package zm.mud.core.network.iac.handler;

import zm.mud.core.network.iac.consts.IACConsts;

public abstract class AbsIACHandler implements IIACCommandHandler{

      @Override
    public byte[] handle(byte[] iacCommand) {
        int commandCode = iacCommand[1] & 0XFF;
        byte[] responseMsg = null;
        switch (commandCode){
            case IACConsts.CMD_WILL -> responseMsg = this.handle_WILL(iacCommand);
            case IACConsts.CMD_WONT -> responseMsg = this.handle_WONT(iacCommand);
            case IACConsts.CMD_DO   -> responseMsg = this.handle_DO(iacCommand);
            case IACConsts.CMD_DONT -> responseMsg = this.handle_DONT(iacCommand);
            default -> responseMsg = null;
        }

        return responseMsg;
    }

    
    protected abstract byte[] handle_WILL(byte[] iacCommand);

    protected abstract byte[] handle_WONT(byte[] iacCommand) ;

    protected abstract byte[] handle_DO(byte[] iacCommand);

    protected abstract byte[] handle_DONT(byte[] iacCommand) ;
  
}

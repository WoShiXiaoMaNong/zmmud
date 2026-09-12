package zm.mud.core.api.oub.cmd;

import java.util.List;

import zm.mud.core.api.oub.IOubCommand;
import zm.mud.core.session.MudSession;

public abstract class AbsOubCommand implements IOubCommand{

    private MudSession session;
    private String originCommandStr;

    public AbsOubCommand(MudSession session, String originCommandStr){
        this.session = session;
        this.originCommandStr = originCommandStr;
    }

    @Override
    public final void exec() {
        List<IOubCommand> transferedMsgs = this.transferCommand(originCommandStr);
        if( transferedMsgs == null || transferedMsgs.isEmpty()){
            return;
        }
        session.pushCommand(transferedMsgs);
        
    }
    

    /**
     * 当返回消息是null 或者 空字符串时，不会触发发送操作
     * @param originCommandStr
     * @return
     */
    protected abstract List<IOubCommand> transferCommand(String originCommandStr);

    @Override
    public String getCommandStr() {
        return  this.originCommandStr;
    }

    public MudSession getSession(){
        return this.session;
    }
    
    
}

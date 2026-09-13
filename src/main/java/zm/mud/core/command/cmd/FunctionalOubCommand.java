package zm.mud.core.command.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.command.func.FuncInfo;
import zm.mud.core.command.func.IFuncCommand;
import zm.mud.core.session.MudSession;

public class FunctionalOubCommand extends AbsOubCommand{

    public FunctionalOubCommand(MudSession session, String originCommandStr) {
        super(session, originCommandStr);
    }

    @Override
    protected List<IOubCommand> transferCommand(String originCommandStr) {
        List<IOubCommand> msgs = new ArrayList<>();
        FuncInfo funcInfo = new FuncInfo(originCommandStr);
        msgs.addAll(this.doFunc(funcInfo));
        return msgs;
    }


    private List<IOubCommand> doFunc(FuncInfo funcInfo){
        IFuncCommand funcCommand = null;
        if( isNumber(funcInfo.getFuncCode())){
            funcCommand = IFuncCommand.getBean("Repeat", funcInfo);
        }else{
            funcCommand = IFuncCommand.getBean(funcInfo.getFuncCode(), funcInfo);
        }
        if(funcCommand != null){
            return funcCommand.doFunc(this.getSession());
        }
        
        return Collections.emptyList();
        
        
    }


   private boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

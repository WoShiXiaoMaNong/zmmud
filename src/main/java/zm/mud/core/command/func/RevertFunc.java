package zm.mud.core.command.func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.command.OubCommandParser;
import zm.mud.core.session.CommandQue;
import zm.mud.core.session.MudSession;

@Component(value = IFuncCommand.FUNC_CMD_SPRING_BEAN_PREFIX + "revert")
@Scope("prototype")
public class RevertFunc implements IFuncCommand {
    private static final Logger logger = LogManager.getLogger(CollectFunc.class);
    private FuncInfo funcInfo;

    @Autowired
    private OubCommandParser parser;

    public RevertFunc(FuncInfo funcInfo) {
        this.funcInfo = funcInfo;
    }

    @Override
    public List<IOubCommand> doFunc(MudSession session,IOubCommand cmd) {
        CommandQue<IOubCommand> historyCmds = session.getHistoryCommandQueue();
        if (historyCmds == null) {
            return Collections.emptyList();
        }
        List<IOubCommand> cmds = new ArrayList<>();

        IOubCommand nextCmd = historyCmds.pollLastCommand();
        while (nextCmd != null) {
            if( !"#revert".equalsIgnoreCase(nextCmd.getCommandStr()) && !"#collect".equalsIgnoreCase(nextCmd.getCommandStr())){
                nextCmd.setRevertMode(true);
                cmds.add(nextCmd);
            }
            nextCmd = historyCmds.pollLastCommand();
        }
        session.stopCollectCmd();
        session.clearCollectCmd();
        session.printToUI("开始回退.");
        return cmds;
    }

}
package zm.mud.core.command.func;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.session.MudSession;

@Component(value = IFuncCommand.FUNC_CMD_SPRING_BEAN_PREFIX + "collect")
@Scope("prototype")
public class CollectFunc implements IFuncCommand {
    private static final Logger logger = LogManager.getLogger(CollectFunc.class);
    private FuncInfo funcInfo;

    public CollectFunc(FuncInfo funcInfo) {
        this.funcInfo = funcInfo;
    }

    @Override
    public List<IOubCommand> doFunc(MudSession session,IOubCommand cmd) {
        session.startCollectCmd();
        session.printToUI("开启指令收集， 别忘了用 #revert 来回退并结束指令收集！");
        return Collections.emptyList();
    }

}
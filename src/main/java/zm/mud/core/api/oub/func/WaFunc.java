package zm.mud.core.api.oub.func;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.api.oub.IOubCommand;
import zm.mud.core.session.MudSession;

@Component(value = IFuncCommand.FUNC_CMD_SPRING_BEAN_PREFIX + "wa")
@Scope("prototype")
public class WaFunc implements IFuncCommand {
    private static final Logger logger = LogManager.getLogger(WaFunc.class);
    private FuncInfo funcInfo;

    public WaFunc(FuncInfo funcInfo) {
        this.funcInfo = funcInfo;
    }

    @Override
    public List<IOubCommand> doFunc(MudSession session) {
        session.echoCommandToUI(this.funcInfo.getOriginCommandStr());
        if( funcInfo.getParams() == null || funcInfo.getParams().length == 0){
            return Collections.emptyList();
        }
        String delayMsStr = funcInfo.getParams()[0];
        try {
            long delayMs = Long.valueOf(delayMsStr);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            logger.error("Do Wa func error!",e);
        }
      
        return Collections.emptyList();
    }

}
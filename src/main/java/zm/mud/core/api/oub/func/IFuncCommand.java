package zm.mud.core.api.oub.func;

import java.util.List;

import zm.mud.core.api.oub.IOubCommand;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;

public interface IFuncCommand {
    public static final String FUNC_CMD_SPRING_BEAN_PREFIX = "FUNC_CMD_SPRING_BEAN_PREFIX_";

    List<IOubCommand> doFunc(MudSession session);


    public static IFuncCommand getBean(String beanIdWithoutPrefix,FuncInfo funcInfo){
        return SpringBeanUtil.getBean(FUNC_CMD_SPRING_BEAN_PREFIX + beanIdWithoutPrefix,IFuncCommand.class,funcInfo);
    }
}

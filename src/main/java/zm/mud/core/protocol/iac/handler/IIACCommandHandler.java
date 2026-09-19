package zm.mud.core.protocol.iac.handler;

import java.util.List;

import zm.mud.core.session.MudSession;

/**
 * 定义bean id的格式：@Component(IACConsts.IAC_HANDLER_BEAN_PREFIX + "9C{这是OPTIONS CODE}")
 * IIACCommandHandler
 */
public interface IIACCommandHandler {
    
     List<byte[]> handle(MudSession session,byte[] iacCommand);
}


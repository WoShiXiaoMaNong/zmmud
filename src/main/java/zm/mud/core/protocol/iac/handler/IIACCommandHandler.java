package zm.mud.core.protocol.iac.handler;

import java.util.List;

/**
 * 定义bean id的格式：@Component(IACConsts.IAC_HANDLER_BEAN_PREFIX + "9C{这是OPTIONS CODE}")
 * IIACCommandHandler
 */
public interface IIACCommandHandler {
    
     List<byte[]> handle(byte[] iacCommand);
}


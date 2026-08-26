package zm.mud.core.protocol.iac.handler;


/**
 * 定义bean id的格式：@Component(IACConsts.IAC_HANDLER_BEAN_PREFIX + "9C{这是OPTIONS CODE}")
 * IIACCommandHandler
 */
public interface IIACCommandHandler {
    
    byte[] handle(byte[] iacCommand);
}


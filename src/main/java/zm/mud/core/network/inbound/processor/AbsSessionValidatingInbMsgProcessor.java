package zm.mud.core.network.inbound.processor;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.session.MudSession;
import zm.mud.core.session.SessionStatus;

public abstract class AbsSessionValidatingInbMsgProcessor implements IInbMsgProcessor{
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(AbsSessionValidatingInbMsgProcessor.class);
    @Override
    public final InbMsg processMessage(InbMsg msg) {
        if(msg == null ){
            return msg;
        }
        if( msg.getSession() == null ){
            msg.setUnconsumable();
            return msg;
        }
        MudSession session = msg.getSession();
        if( SessionStatus.isAvailable(session.getStatus())){
            return this.doProcess(msg);
        }else{
            logger.debug("Session is inavailable! " +  session.getSessionId());
        }
        return msg;
    }

    protected abstract InbMsg doProcess(InbMsg msg);

}

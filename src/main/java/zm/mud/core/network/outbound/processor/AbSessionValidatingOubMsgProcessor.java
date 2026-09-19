package zm.mud.core.network.outbound.processor;

import zm.mud.core.network.outbound.message.OubMsg;
import zm.mud.core.session.MudSession;
import zm.mud.core.session.SessionStatus;

public abstract class AbSessionValidatingOubMsgProcessor implements IOubMsgProcessor{
     private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(AbSessionValidatingOubMsgProcessor.class);
    @Override
    public final boolean processMessage(OubMsg msg) {
         if(msg == null || msg.getSession() == null ){
            return true;
        }
        MudSession session = msg.getSession();
        if( SessionStatus.isAvailable(session.getStatus())){
            return this.doProcess(msg);
        }else{
            logger.debug("Session is inavailable! " +  session.getSessionId());
        }
        return true;
    }

    protected abstract boolean doProcess(OubMsg msg);
}

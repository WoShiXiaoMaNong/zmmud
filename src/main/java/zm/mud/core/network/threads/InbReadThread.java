package zm.mud.core.network.threads;



import zm.mud.core.network.inbound.reader.InbMsgReader;
import zm.mud.core.session.MudSession;
import zm.mud.utils.SpringBeanUtil;


public class InbReadThread extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbReadThread.class);


    private InbMsgReader reader;

    public InbReadThread(MudSession session){
        super(session);
        this.reader = SpringBeanUtil.getBean(InbMsgReader.class);
    }

    @Override
    public boolean doRun() {
        try {
            reader.handleByte(this.getSession(), this.getSession().getClient().read(), this.getSession().getClient().getCharset());
            return true;
        } catch (Exception e) {
            logger.error("Failed to read from server", e);
            throw e;
        }

    }

}

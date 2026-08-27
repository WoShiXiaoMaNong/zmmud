package zm.mud.pkuxkx.gmcp;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;



import zm.mud.core.protocol.gmcp.IGMCPOnMessage;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;


@Component
public class GMCPMsglisener implements IGMCPOnMessage {
    private static final Logger logger = LogManager.getLogger(GMCPMsglisener.class);

    @Autowired
    private GMCPContext gmcpContext;

    @Autowired
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(String packageName, String jsonPayload) {
        IGMCPMsgHandler parser = null;
        try{
            logger.debug("Received GMCP message: " + packageName + " with payload: " + jsonPayload);
            parser = (IGMCPMsgHandler) applicationContext.getBean(packageName);
            parser.parse(packageName, jsonPayload, gmcpContext);
        }catch(Exception e){
            logger.warn("No parser found for package: " + packageName);
        }
    }



}

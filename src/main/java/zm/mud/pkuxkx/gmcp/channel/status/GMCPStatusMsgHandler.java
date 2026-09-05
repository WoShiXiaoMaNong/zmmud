package zm.mud.pkuxkx.gmcp.channel.status;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import zm.mud.core.session.MudSession;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.util.AnsiTextUtil;


@Component("GMCP.Status")
public class GMCPStatusMsgHandler implements IGMCPMsgHandler {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(GMCPStatusMsgHandler.class);
    @Autowired
    private ZmMudUI ui;

    @Autowired
    private AnsiTextUtil AnsiTextUtil;

    @Override
    public void parse(MudSession session,String packageName, String jsonPayload) {
        Map<String, Object> packageDataMap =  JSON.parseObject(jsonPayload, Map.class);
        if(packageDataMap != null){
            GMCPContext gmcpContext = session.getGmcpContext();
            for(Map.Entry<String, Object> entry : packageDataMap.entrySet()){
                 if(  entry.getValue() != null && entry.getValue() instanceof String ){
                    gmcpContext.put(packageName,entry.getKey(), AnsiTextUtil.cleanStartsWith((String)entry.getValue()) );
                }else{
                    gmcpContext.put(packageName,entry.getKey(), entry.getValue());
                }
                
                
            }
            Object name = packageDataMap.get("name");
            Object id = packageDataMap.get("id");
            if( name != null && id != null){
                this.ui.setTitle(session, String.format(" >%s(%s)<", name,id));
                this.ui.setCurrentUserName(session, String.format(" [%s]: ", name));
                this.ui.printlnToScreen(session, String.format("GMCP.Status:  %s(%s)",packageName,jsonPayload));
                logger.info("GMCP.Status: {}: {}",packageName,jsonPayload);
            }

          
        }
      
    }

}

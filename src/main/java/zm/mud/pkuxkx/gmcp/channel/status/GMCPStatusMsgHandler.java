package zm.mud.pkuxkx.gmcp.channel.status;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;
import zm.mud.ui.ZmMudUI;


@Component("GMCP.Status")
public class GMCPStatusMsgHandler implements IGMCPMsgHandler {

    @Autowired
    private ZmMudUI ui;

    @Override
    public void parse(String packageName, String jsonPayload, GMCPContext gmcpContext) {
        Map<String, Object> packageDataMap =  JSON.parseObject(jsonPayload, Map.class);
        if(packageDataMap != null){
            for(Map.Entry<String, Object> entry : packageDataMap.entrySet()){
                gmcpContext.putStatus(entry.getKey(), entry.getValue());
            }
            Object name = packageDataMap.get("name");
            Object id = packageDataMap.get("id");
            if( name != null && id != null){
                this.ui.setTitle( String.format("%s（%s）", name,id));
            }

          
        }
      
    }

}

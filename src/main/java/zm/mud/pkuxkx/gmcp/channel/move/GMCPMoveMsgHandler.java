package zm.mud.pkuxkx.gmcp.channel.move;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import zm.mud.core.session.MudSession;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;

@Component("GMCP.Move")
public class GMCPMoveMsgHandler implements IGMCPMsgHandler {
    @Override
    public void parse(MudSession session,String packageName, String jsonPayload) {
        List<Map<String,Object>> room = JSON.parseObject(jsonPayload,  new TypeReference<List<Map<String,Object>>>() {});

        if(room != null && !room.isEmpty()) {
            for(Map<String,Object> r : room) {
                Object result = r.get("result");
                if("TRUE".equalsIgnoreCase(String.valueOf(result))) {
                    for(Entry<String,Object> entry : r.entrySet()){
                        GMCPContext gmcpContext = session.getGmcpContext();
                        gmcpContext.put(packageName, entry.getKey(), entry.getValue());
                    }   
                }
            }
            
        }
    
    }

}

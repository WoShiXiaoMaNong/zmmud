package zm.mud.pkuxkx.gmcp.channel.move;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;

@Component("GMCP.Move")
public class GMCPMoveMsgHandler implements IGMCPMsgHandler {
    @Override
    public void parse(String packageName, String jsonPayload, GMCPContext gmcpContext) {
        List<PkuxkxRoom> room = JSON.parseObject(jsonPayload,  new TypeReference<List<PkuxkxRoom>>() {});

        if(room != null && !room.isEmpty()) {
            for(PkuxkxRoom r : room) {
                if(r.getResult()) {
                    gmcpContext.setRoom(r);
                }
            }
            
        }
    
    }

}

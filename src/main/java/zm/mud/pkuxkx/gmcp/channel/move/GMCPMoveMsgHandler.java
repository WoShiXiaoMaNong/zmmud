package zm.mud.pkuxkx.gmcp.channel.move;

import java.util.List;

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
        List<PkuxkxRoom> room = JSON.parseObject(jsonPayload,  new TypeReference<List<PkuxkxRoom>>() {});

        if(room != null && !room.isEmpty()) {
            for(PkuxkxRoom r : room) {
                if(r.getResult()) {
                    GMCPContext gmcpContext = session.getGmcpContext();
                    gmcpContext.setRoom(r);
                }
            }
            
        }
    
    }

}

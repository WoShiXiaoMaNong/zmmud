package zm.mud.core.network.outbound.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import zm.mud.core.api.OubMsgService;
import zm.mud.core.automation.alias.Alias;
import zm.mud.core.automation.alias.AliasService;
import zm.mud.core.network.outbound.message.OubMsg;

@Service
public class AliasProcessor implements IOubMsgProcessor ,Ordered{

    @Autowired
    private AliasService aliasService;
 
    @Autowired
    private OubMsgService oubMsgService;

    @Override
    public boolean processMessage(OubMsg msg) {
        String msgContent = msg.getContent();
        Alias a = this.aliasService.getAlias(msgContent);
        boolean isAlias = a != null;
        if(isAlias){
            this.aliasService.doAlias(a, this.oubMsgService);
            return true;
        }else{
            return false;
        }
    }

   @Override
    public int getOrder() {
        return 1;
    }
}

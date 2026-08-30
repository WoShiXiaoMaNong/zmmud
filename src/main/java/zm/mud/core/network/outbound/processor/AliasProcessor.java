package zm.mud.core.network.outbound.processor;

import java.util.ArrayList;
import java.util.List;

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
        if(msg == null){
            return false;
        }
        String msgContent = msg.getContent();
        if( msgContent == null || msgContent.isEmpty()){
            return false;
        }

        String[] aliasStrs = msgContent.split(";");

        boolean includeAlias = false;

        for(String aliasStr : aliasStrs){
            boolean isAlias = this.process(msg,aliasStr);
            if(!includeAlias){
                includeAlias = isAlias;
            }
        }

        // 当发现包含了 alias的时候，该消息就认为被处理结束，后续process 不应该再消费该消息。
        boolean shouldCountinue = includeAlias;

        return shouldCountinue;
       
    }

    private boolean process(OubMsg msg,String aliasStr){
        // Alias: Alia + params
        String[] msgs = aliasStr.split(" ");
        String [] args = null;
        if(msgs.length > 1){
            args = new String[msgs.length - 1];
            for(int i = 1; i < msgs.length; i++){
                args[i - 1] = msgs[i];
            }
        }
        Alias a = this.aliasService.getAlias(msgs[0]);
        boolean isAlias = a != null;
        if(isAlias){
            this.aliasService.doAlias(msg.getSession(),a, this.oubMsgService,args);
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

package zm.mud.core.automation.alias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.TypeReference;

import jakarta.annotation.PostConstruct;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.cfg.CustomCfgLoader;
import zm.mud.core.session.MudSession;


@Lazy
@Service
public class AliasService {



    private Map<String,Alias> aliasMap = new HashMap<>();

  

    public void doAlias(MudSession session,Alias alias, OubMsgService oubMsgService, String ...params) {
        
        String aliasCommand = alias.getAliasCommand();
        if( params != null){
            aliasCommand = aliasCommand + " " + String.join(" ",params);
        }
        oubMsgService.send(session,aliasCommand );
    }

    /**
     * return null if alias not found
     * @param aliasName
     * @return
     */
    public Alias getAlias(String aliasName) {
        return aliasMap.get(aliasName);
    }


    @PostConstruct
    public void reload() {
        List<Alias> aliasList = (List<Alias>) CustomCfgLoader.loadUIConfig("pkuxkx", "alias",
                    new TypeReference<List<Alias>>(){});
        
        if(aliasList == null || aliasList.isEmpty()){
            return;
        }
        this.aliasMap.clear();
        for(Alias a : aliasList){
            aliasMap.put(a.getAliasName(), a);
        }
       
    }
    
}

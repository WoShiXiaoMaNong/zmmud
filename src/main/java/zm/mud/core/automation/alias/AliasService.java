package zm.mud.core.automation.alias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import zm.mud.core.api.OubMsgService;

@Lazy
@Service
public class AliasService {

    @Autowired
    private AliasLoader aliasLoader;

    private Map<String,Alias> aliasMap = new HashMap<>();

  

    public void doAlias(Alias alias, OubMsgService oubMsgService) {
        
        String aliasCommand = alias.getAliasCommand();
        oubMsgService.send(aliasCommand);
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
    public void init() {
        List<Alias> aliasList = aliasLoader.loadAlias();
        for(Alias a : aliasList){
            aliasMap.put(a.getAliasName(), a);
        }
       
    }
    
}

package zm.mud.core.automation.action;

import java.util.HashMap;
import java.util.Map;

import zm.mud.core.session.MudSession;

public class ActionContext {
    
    private Map<String,Object> variables;

    private MudSession session;

    public ActionContext(MudSession session){
        this.variables = new HashMap<>();
        this.session = session;
    }


    public void put(String k, Object v){
        this.variables.put(k, v);
    }

    public Object get(String k){
        if( k == null){
            return null;
        }

        return this.variables.get(k);
    }


    public MudSession getSession() {
        return session;
    }


    
}

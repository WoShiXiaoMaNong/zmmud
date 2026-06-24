package zm.mud.core.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    
    @Autowired
    private OubMsgService oubMsgService;

    public void quit(){
        //this.oubMsgService.send("quit");
    }
}
